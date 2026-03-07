package com.jkingai.pokertutor.model;

/**
 * Valid player actions in a poker betting round.
 */
public enum PlayerAction {
    BET,      // Place a bet when no outstanding bet exists
    CALL,     // Match the current outstanding bet
    FOLD,     // Surrender the hand
    RAISE,    // Increase the current outstanding bet
    CHECK,    // Pass when no outstanding bet exists
    ALL_IN    // Bet all remaining chips
}
