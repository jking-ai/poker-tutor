package com.jkingai.pokertutor.dto;

import com.jkingai.pokertutor.model.PlayerAction;

/**
 * Request DTO for submitting a player action.
 */
public record ActionRequest(
        PlayerAction action,
        Integer amount  // Required for BET, RAISE; null/ignored for CALL, FOLD, CHECK
) {
    // TODO: Add validation: amount must be positive when action is BET or RAISE
}
