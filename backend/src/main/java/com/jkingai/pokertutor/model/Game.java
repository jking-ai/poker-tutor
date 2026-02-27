package com.jkingai.pokertutor.model;

import java.util.List;

/**
 * Core game state entity for a heads-up Texas Hold'em game.
 * Tracks all information needed to represent the current state of play.
 */
public class Game {

    // TODO: Implement fields:
    //   - String gameId
    //   - List<Player> players (index 0 = human, index 1 = AI opponent)
    //   - Deck deck
    //   - List<Card> communityCards (0-5 cards depending on phase)
    //   - int pot
    //   - GamePhase phase
    //   - int currentPlayerIndex (whose turn it is)
    //   - int dealerIndex (alternates each hand)
    //   - int handNumber
    //   - int smallBlind
    //   - int bigBlind
    //   - List<HandHistory> handHistory
    //   - ActionDetail lastAction

    // TODO: Implement constructors, getters, setters

    // TODO: Implement helper methods:
    //   - getCurrentPlayer()
    //   - getOpponent()
    //   - getDealer()
    //   - isPlayerTurn()
    //   - addToPot(int amount)
    //   - advancePhase()
}
