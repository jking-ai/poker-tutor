package com.jkingai.pokertutor.exception;

import com.jkingai.pokertutor.model.PlayerAction;

import java.util.List;

/**
 * Thrown when a player submits an action that is not valid for the current game state.
 * Maps to HTTP 400.
 */
public class InvalidActionException extends RuntimeException {

    private final PlayerAction attemptedAction;
    private final List<PlayerAction> validActions;

    public InvalidActionException(String message, PlayerAction attemptedAction, List<PlayerAction> validActions) {
        super(message);
        this.attemptedAction = attemptedAction;
        this.validActions = validActions;
    }

    public PlayerAction getAttemptedAction() {
        return attemptedAction;
    }

    public List<PlayerAction> getValidActions() {
        return validActions;
    }
}
