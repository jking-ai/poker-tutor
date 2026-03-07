package com.jkingai.pokertutor.controller;

import com.jkingai.pokertutor.dto.CoachingResponse;
import com.jkingai.pokertutor.dto.OddsResponse;
import com.jkingai.pokertutor.model.Game;
import com.jkingai.pokertutor.model.Player;
import com.jkingai.pokertutor.service.CoachAgentService;
import com.jkingai.pokertutor.service.GameService;
import com.jkingai.pokertutor.service.HandEvaluatorService;
import com.jkingai.pokertutor.service.OddsCalculatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/games/{id}")
public class CoachingController {

    private final GameService gameService;
    private final CoachAgentService coachAgentService;
    private final OddsCalculatorService oddsCalculatorService;
    private final HandEvaluatorService handEvaluatorService;

    public CoachingController(GameService gameService, CoachAgentService coachAgentService,
                              OddsCalculatorService oddsCalculatorService,
                              HandEvaluatorService handEvaluatorService) {
        this.gameService = gameService;
        this.coachAgentService = coachAgentService;
        this.oddsCalculatorService = oddsCalculatorService;
        this.handEvaluatorService = handEvaluatorService;
    }

    @GetMapping("/coaching")
    public ResponseEntity<CoachingResponse> getCoaching(@PathVariable String id) {
        Game game = gameService.getGame(id);
        CoachingResponse response = coachAgentService.getCoachingAdvice(game);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/odds")
    public ResponseEntity<OddsResponse> getOdds(@PathVariable String id) {
        Game game = gameService.getGame(id);
        Player human = game.getHumanPlayer();
        Player opponent = game.getAiPlayer();

        int costToCall = Math.max(0, opponent.getCurrentBet() - human.getCurrentBet());
        double potOdds = oddsCalculatorService.calculatePotOdds(game.getPot(), costToCall);
        String potOddsRatio = oddsCalculatorService.formatPotOddsRatio(game.getPot(), costToCall);

        OddsCalculatorService.EquityResult equity =
                oddsCalculatorService.estimateEquity(human.getHoleCards(), game.getCommunityCards(), 500);

        OddsCalculatorService.OutsResult outsResult =
                oddsCalculatorService.countOuts(human.getHoleCards(), game.getCommunityCards());

        boolean isNut = game.getCommunityCards().size() >= 3
                && oddsCalculatorService.isNut(human.getHoleCards(), game.getCommunityCards());

        String nutHand = game.getCommunityCards().size() >= 3
                ? handEvaluatorService.identifyNutHand(game.getCommunityCards())
                : "Unknown (pre-flop)";

        var outsDetails = outsResult.details().stream()
                .map(d -> new OddsResponse.OutDetail(d.card(), d.count(), d.description()))
                .toList();

        return ResponseEntity.ok(new OddsResponse(
                game.getPot(), costToCall, potOdds, potOddsRatio,
                equity.winProbability(), outsResult.totalOuts(), outsDetails,
                equity.winProbability(), equity.tieProbability(), equity.loseProbability(),
                isNut, nutHand
        ));
    }
}
