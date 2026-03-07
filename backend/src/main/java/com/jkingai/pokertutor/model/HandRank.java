package com.jkingai.pokertutor.model;

/**
 * Poker hand rankings from highest (ROYAL_FLUSH) to lowest (HIGH_CARD).
 * Ordinal order matches ranking: lower ordinal = stronger hand.
 */
public enum HandRank {
    ROYAL_FLUSH("Royal Flush"),
    STRAIGHT_FLUSH("Straight Flush"),
    FOUR_OF_A_KIND("Four of a Kind"),
    FULL_HOUSE("Full House"),
    FLUSH("Flush"),
    STRAIGHT("Straight"),
    THREE_OF_A_KIND("Three of a Kind"),
    TWO_PAIR("Two Pair"),
    PAIR("Pair"),
    HIGH_CARD("High Card");

    private final String displayName;

    HandRank(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
