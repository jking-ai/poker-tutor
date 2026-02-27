package com.jkingai.pokertutor.model;

import java.util.List;

/**
 * Represents a player in the poker game (either human or AI opponent).
 */
public class Player {

    // TODO: Implement fields:
    //   - String name
    //   - int chips (current chip stack)
    //   - List<Card> holeCards (2 private cards)
    //   - int currentBet (amount bet in the current betting round)
    //   - boolean folded
    //   - boolean isDealer

    // TODO: Implement constructors, getters, setters

    // TODO: Implement helper methods:
    //   - placeBet(int amount) -- deducts from chips, adds to currentBet
    //   - resetBet() -- sets currentBet to 0 (at start of new betting round)
    //   - addChips(int amount) -- adds winnings to chip stack
    //   - fold() -- sets folded = true
    //   - isAllIn() -- returns true if chips == 0 and not folded
}
