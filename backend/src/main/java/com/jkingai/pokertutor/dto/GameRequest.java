package com.jkingai.pokertutor.dto;

import jakarta.validation.constraints.*;

public record GameRequest(
        @NotBlank(message = "Player name is required")
        @Size(min = 1, max = 50, message = "Player name must be 1-50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9\\s\\-']+$", message = "Player name contains invalid characters")
        String playerName,

        @Min(value = 100, message = "Starting chips must be at least 100")
        @Max(value = 100000, message = "Starting chips cannot exceed 100,000")
        int startingChips,

        @Min(value = 1, message = "Small blind must be at least 1")
        @Max(value = 10000, message = "Small blind cannot exceed 10,000")
        int smallBlind,

        @Min(value = 1, message = "Big blind must be at least 1")
        @Max(value = 10000, message = "Big blind cannot exceed 10,000")
        int bigBlind
) {}
