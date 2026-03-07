package com.jkingai.pokertutor.service;

import com.jkingai.pokertutor.dto.CoachingResponse;
import com.jkingai.pokertutor.model.Game;
import com.jkingai.pokertutor.model.Player;
import com.jkingai.pokertutor.model.PlayerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

@Service
public class CoachAgentService {

    private static final Logger log = LoggerFactory.getLogger(CoachAgentService.class);
    private static final int TIMEOUT_SECONDS = 15;
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private final OddsCalculatorService oddsCalculatorService;
    private final HandEvaluatorService handEvaluatorService;
    private final ChatClient chatClient;
    private final RateLimitService rateLimitService;

    public CoachAgentService(OddsCalculatorService oddsCalculatorService,
                             HandEvaluatorService handEvaluatorService,
                             @Autowired(required = false) @Qualifier("coachChatClient") ChatClient chatClient,
                             RateLimitService rateLimitService) {
        this.oddsCalculatorService = oddsCalculatorService;
        this.handEvaluatorService = handEvaluatorService;
        this.chatClient = chatClient;
        this.rateLimitService = rateLimitService;
    }

    public CoachingResponse getCoachingAdvice(Game game) {
        rateLimitService.checkCoachingAllowed(game);
        game.incrementCoachingCallsThisHand();

        Player human = game.getHumanPlayer();
        Player opponent = game.getAiPlayer();

        // Calculate math
        int costToCall = Math.max(0, opponent.getCurrentBet() - human.getCurrentBet());
        double potOdds = oddsCalculatorService.calculatePotOdds(game.getPot(), costToCall);
        String potOddsRatio = oddsCalculatorService.formatPotOddsRatio(game.getPot(), costToCall);

        HandEvaluatorService.HandEvaluation handEval =
                handEvaluatorService.evaluateBestHand(human.getHoleCards(), game.getCommunityCards());

        OddsCalculatorService.OutsResult outsResult =
                oddsCalculatorService.countOuts(human.getHoleCards(), game.getCommunityCards());

        OddsCalculatorService.EquityResult equity =
                oddsCalculatorService.estimateEquity(human.getHoleCards(), game.getCommunityCards(), 500);

        int nutDistance = game.getCommunityCards().size() >= 3
                ? oddsCalculatorService.calculateNutDistance(human.getHoleCards(), game.getCommunityCards())
                : -1;

        String nutHand = game.getCommunityCards().size() >= 3
                ? handEvaluatorService.identifyNutHand(game.getCommunityCards())
                : "Unknown (pre-flop)";

        // Generate deterministic coaching advice (no LLM for PoC)
        PlayerAction recommended = determineRecommendation(equity, potOdds, costToCall, outsResult);
        String confidence = determineConfidence(equity);
        String advice = generateAdvice(handEval, equity, potOdds, potOddsRatio, outsResult, costToCall, recommended);

        String outsDescription = outsResult.details().stream()
                .map(OddsCalculatorService.OutDetail::description)
                .reduce((a, b) -> a + "; " + b)
                .orElse("No outs identified");

        // Ask AI coach for personalized advice (if AI enabled and within rate limit)
        String aiCoachAdvice = null;
        if (rateLimitService.isAiCallAllowed(game)) {
            aiCoachAdvice = getAiCoachAdvice(game, handEval, equity, potOddsRatio,
                    outsResult, costToCall, recommended);
            if (aiCoachAdvice != null) {
                game.incrementAiCallCount();
            }
        } else {
            log.info("AI call limit reached for game {}, returning deterministic coaching only", game.getGameId());
        }

        return new CoachingResponse(
                advice,
                recommended,
                confidence,
                new CoachingResponse.HandStrengthDto(handEval.rank(), handEval.description(), nutDistance),
                new CoachingResponse.OddsDto(potOdds, potOddsRatio, equity.winProbability(), outsResult.totalOuts(), outsDescription),
                new CoachingResponse.ExplanationDto(
                        game.getPhase().name() + " with " + handEval.rank().getDisplayName(),
                        String.format("Equity: %.0f%% | Pot odds: %s | Outs: %d",
                                equity.winProbability() * 100, potOddsRatio, outsResult.totalOuts()),
                        advice
                ),
                aiCoachAdvice
        );
    }

    private PlayerAction determineRecommendation(OddsCalculatorService.EquityResult equity,
                                                  double potOdds, int costToCall,
                                                  OddsCalculatorService.OutsResult outs) {
        double winProb = equity.winProbability();

        if (costToCall == 0) {
            if (winProb > 0.7) return PlayerAction.BET;
            return PlayerAction.CHECK;
        }

        if (winProb > 0.7) return PlayerAction.RAISE;
        if (winProb > potOdds) return PlayerAction.CALL;
        if (outs.totalOuts() >= 8 && winProb > 0.25) return PlayerAction.CALL;
        return PlayerAction.FOLD;
    }

    private String determineConfidence(OddsCalculatorService.EquityResult equity) {
        double winProb = equity.winProbability();
        if (winProb > 0.7) return "HIGH";
        if (winProb > 0.45) return "MEDIUM";
        return "LOW";
    }

    private String generateAdvice(HandEvaluatorService.HandEvaluation handEval,
                                   OddsCalculatorService.EquityResult equity,
                                   double potOdds, String potOddsRatio,
                                   OddsCalculatorService.OutsResult outs,
                                   int costToCall, PlayerAction recommended) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("You have %s. ", handEval.rank().getDisplayName()));

        if (costToCall > 0) {
            sb.append(String.format("The pot is offering %s odds. ", potOddsRatio));
            sb.append(String.format("You need %.0f%% equity to call profitably, ", potOdds * 100));
            sb.append(String.format("and you have approximately %.0f%% equity. ", equity.winProbability() * 100));

            if (equity.winProbability() > potOdds) {
                sb.append("This is a profitable call. ");
            } else {
                sb.append("This call is not profitable based on pot odds alone. ");
            }
        } else {
            sb.append(String.format("Your equity is approximately %.0f%%. ", equity.winProbability() * 100));
        }

        if (outs.totalOuts() > 0) {
            sb.append(String.format("You have %d outs to improve your hand. ", outs.totalOuts()));
        }

        sb.append(String.format("Recommendation: %s.", recommended.name()));

        return sb.toString();
    }

    private String getAiCoachAdvice(Game game,
                                     HandEvaluatorService.HandEvaluation handEval,
                                     OddsCalculatorService.EquityResult equity,
                                     String potOddsRatio,
                                     OddsCalculatorService.OutsResult outs,
                                     int costToCall,
                                     PlayerAction recommended) {
        if (chatClient == null) {
            return null;
        }

        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("The player is in the ").append(game.getPhase()).append(" phase.\n");
            prompt.append("Their best hand: ").append(handEval.rank().getDisplayName())
                    .append(" (").append(handEval.description()).append(")\n");
            prompt.append("Equity: ").append(String.format("%.0f%%", equity.winProbability() * 100)).append("\n");
            if (costToCall > 0) {
                prompt.append("Cost to call: $").append(costToCall).append("\n");
                prompt.append("Pot odds: ").append(potOddsRatio).append("\n");
            }
            if (outs.totalOuts() > 0) {
                prompt.append("Outs: ").append(outs.totalOuts()).append("\n");
            }
            prompt.append("Pot: $").append(game.getPot()).append("\n");
            prompt.append("Their chips: $").append(game.getHumanPlayer().getChips()).append("\n");
            prompt.append("Math-based recommendation: ").append(recommended.name()).append("\n");
            prompt.append("\nGive your coaching advice for this situation.");

            Future<String> future = executor.submit(() ->
                    chatClient.prompt().user(prompt.toString()).call().content()
            );
            String response = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("Coach AI responded: {}", response);
            return response;
        } catch (Exception e) {
            log.warn("Coach AI error: {}", e.getMessage());
            return null;
        }
    }
}
