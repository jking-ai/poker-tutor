package com.jkingai.pokertutor.model;

import java.util.List;

public class Hand {

    private final List<Card> holeCards;
    private final List<Card> bestFiveCards;
    private final HandRank rank;

    public Hand(List<Card> holeCards) {
        this.holeCards = List.copyOf(holeCards);
        this.bestFiveCards = null;
        this.rank = null;
    }

    public Hand(List<Card> holeCards, List<Card> bestFiveCards, HandRank rank) {
        this.holeCards = List.copyOf(holeCards);
        this.bestFiveCards = List.copyOf(bestFiveCards);
        this.rank = rank;
    }

    public List<Card> getHoleCards() { return holeCards; }
    public List<Card> getBestFiveCards() { return bestFiveCards; }
    public HandRank getRank() { return rank; }

    @Override
    public String toString() {
        if (rank != null) {
            return rank.getDisplayName() + ": " + bestFiveCards;
        }
        return "Unevaluated hand: " + holeCards;
    }
}
