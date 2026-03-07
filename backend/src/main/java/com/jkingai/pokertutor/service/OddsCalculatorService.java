package com.jkingai.pokertutor.service;

import com.jkingai.pokertutor.model.Card;
import com.jkingai.pokertutor.model.HandRank;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OddsCalculatorService {

    private final HandEvaluatorService handEvaluatorService;

    public OddsCalculatorService(HandEvaluatorService handEvaluatorService) {
        this.handEvaluatorService = handEvaluatorService;
    }

    public double calculatePotOdds(int potSize, int costToCall) {
        if (costToCall <= 0) return 0.0;
        return (double) costToCall / (potSize + costToCall);
    }

    public String formatPotOddsRatio(int potSize, int costToCall) {
        if (costToCall <= 0) return "N/A";
        double ratio = (double) potSize / costToCall;
        return String.format("%.1f:1", ratio);
    }

    public OutsResult countOuts(List<Card> holeCards, List<Card> communityCards) {
        if (communityCards == null || communityCards.size() < 3) {
            return new OutsResult(0, List.of());
        }

        Set<Card> knownCards = new HashSet<>(holeCards);
        knownCards.addAll(communityCards);

        HandEvaluatorService.HandEvaluation currentEval =
                handEvaluatorService.evaluateBestHand(holeCards, communityCards);

        List<Card> remainingCards = buildRemainingDeck(knownCards);
        Map<String, Integer> outsByType = new LinkedHashMap<>();
        int totalOuts = 0;

        for (Card card : remainingCards) {
            List<Card> newCommunity = new ArrayList<>(communityCards);
            newCommunity.add(card);
            HandEvaluatorService.HandEvaluation newEval =
                    handEvaluatorService.evaluateBestHand(holeCards, newCommunity);

            if (newEval.rank().ordinal() < currentEval.rank().ordinal()) {
                String improvement = newEval.rank().getDisplayName();
                outsByType.merge(improvement, 1, Integer::sum);
                totalOuts++;
            }
        }

        List<OutDetail> details = outsByType.entrySet().stream()
                .map(e -> new OutDetail(e.getKey(), e.getValue(), e.getValue() + " cards to make " + e.getKey()))
                .toList();

        return new OutsResult(totalOuts, details);
    }

    public EquityResult estimateEquity(List<Card> holeCards, List<Card> communityCards, int simulations) {
        Set<Card> knownCards = new HashSet<>(holeCards);
        if (communityCards != null) knownCards.addAll(communityCards);

        List<Card> remainingCards = buildRemainingDeck(knownCards);
        Random random = new Random();

        int wins = 0, ties = 0, losses = 0;

        for (int i = 0; i < simulations; i++) {
            Collections.shuffle(remainingCards, random);
            int idx = 0;

            // Deal opponent cards
            List<Card> oppHole = List.of(remainingCards.get(idx++), remainingCards.get(idx++));

            // Deal remaining community cards
            List<Card> fullCommunity = new ArrayList<>(communityCards != null ? communityCards : List.of());
            while (fullCommunity.size() < 5) {
                fullCommunity.add(remainingCards.get(idx++));
            }

            int result = handEvaluatorService.compareHands(holeCards, oppHole, fullCommunity);
            if (result > 0) wins++;
            else if (result < 0) losses++;
            else ties++;
        }

        return new EquityResult(
                (double) wins / simulations,
                (double) ties / simulations,
                (double) losses / simulations
        );
    }

    public boolean isNut(List<Card> holeCards, List<Card> communityCards) {
        if (communityCards == null || communityCards.size() < 3) return false;

        Set<Card> knownCards = new HashSet<>(holeCards);
        knownCards.addAll(communityCards);

        HandEvaluatorService.HandEvaluation playerEval =
                handEvaluatorService.evaluateBestHand(holeCards, communityCards);

        List<Card> remaining = buildRemainingDeck(knownCards);

        // Check all possible opponent hole card combinations
        for (int i = 0; i < remaining.size(); i++) {
            for (int j = i + 1; j < remaining.size(); j++) {
                List<Card> oppHole = List.of(remaining.get(i), remaining.get(j));
                HandEvaluatorService.HandEvaluation oppEval =
                        handEvaluatorService.evaluateBestHand(oppHole, communityCards);
                if (handEvaluatorService.compareEvaluations(oppEval, playerEval) > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public int calculateNutDistance(List<Card> holeCards, List<Card> communityCards) {
        if (communityCards == null || communityCards.size() < 3) return -1;

        HandEvaluatorService.HandEvaluation playerEval =
                handEvaluatorService.evaluateBestHand(holeCards, communityCards);

        Set<Card> knownCards = new HashSet<>(holeCards);
        knownCards.addAll(communityCards);
        List<Card> remaining = buildRemainingDeck(knownCards);

        HandRank bestPossible = playerEval.rank();
        for (int i = 0; i < remaining.size(); i++) {
            for (int j = i + 1; j < remaining.size(); j++) {
                List<Card> oppHole = List.of(remaining.get(i), remaining.get(j));
                HandEvaluatorService.HandEvaluation oppEval =
                        handEvaluatorService.evaluateBestHand(oppHole, communityCards);
                if (oppEval.rank().ordinal() < bestPossible.ordinal()) {
                    bestPossible = oppEval.rank();
                }
            }
        }

        return playerEval.rank().ordinal() - bestPossible.ordinal();
    }

    private List<Card> buildRemainingDeck(Set<Card> knownCards) {
        List<Card> remaining = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                Card c = new Card(rank, suit);
                if (!knownCards.contains(c)) {
                    remaining.add(c);
                }
            }
        }
        return remaining;
    }

    public record OutsResult(int totalOuts, List<OutDetail> details) {}
    public record OutDetail(String card, int count, String description) {}
    public record EquityResult(double winProbability, double tieProbability, double loseProbability) {}
}
