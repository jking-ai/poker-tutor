package com.jkingai.pokertutor.dto;

import com.jkingai.pokertutor.model.PlayerAction;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ActionRequest(
        @NotNull(message = "Action is required")
        PlayerAction action,

        @Min(value = 1, message = "Bet amount must be positive")
        Integer amount  // Required for BET, RAISE; null/ignored for CALL, FOLD, CHECK
) {}
