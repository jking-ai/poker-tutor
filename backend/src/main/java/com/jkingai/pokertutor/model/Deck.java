package com.jkingai.pokertutor.model;

import java.util.List;

/**
 * Represents a standard 52-card deck.
 * Supports shuffling, dealing, and resetting.
 */
public class Deck {

    // TODO: Implement fields:
    //   - List<Card> cards (the remaining cards in the deck)

    /**
     * Create a new full 52-card deck (unshuffled).
     */
    public Deck() {
        // TODO: Initialize with all 52 cards (13 ranks x 4 suits)
    }

    /**
     * Shuffle the deck using Fisher-Yates algorithm.
     */
    public void shuffle() {
        // TODO: Implement shuffle using Collections.shuffle() or Fisher-Yates
        throw new UnsupportedOperationException("TODO: Implement shuffle");
    }

    /**
     * Deal (remove and return) the top card from the deck.
     */
    public Card deal() {
        // TODO: Remove and return the top card
        // TODO: Throw IllegalStateException if deck is empty
        throw new UnsupportedOperationException("TODO: Implement deal");
    }

    /**
     * Deal multiple cards from the top of the deck.
     */
    public List<Card> deal(int count) {
        // TODO: Deal 'count' cards and return as a list
        throw new UnsupportedOperationException("TODO: Implement deal(count)");
    }

    /**
     * Reset the deck to a full 52-card state.
     */
    public void reset() {
        // TODO: Re-initialize with all 52 cards
        throw new UnsupportedOperationException("TODO: Implement reset");
    }

    /**
     * Return the number of remaining cards.
     */
    public int remaining() {
        // TODO: Return cards.size()
        throw new UnsupportedOperationException("TODO: Implement remaining");
    }
}
