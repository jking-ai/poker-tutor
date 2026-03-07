package com.jkingai.pokertutor.model;

/**
 * Phases of a Texas Hold'em hand.
 * The game progresses through these phases sequentially.
 */
public enum GamePhase {
    PRE_FLOP,   // Hole cards dealt, first betting round
    FLOP,       // 3 community cards dealt, second betting round
    TURN,       // 4th community card dealt, third betting round
    RIVER,      // 5th community card dealt, final betting round
    SHOWDOWN    // Both players reveal cards, winner determined
}
