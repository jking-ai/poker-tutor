package com.jkingai.pokertutor.dto;

/**
 * Request DTO for creating a new game.
 */
public record GameRequest(
        String playerName,
        int startingChips,
        int smallBlind,
        int bigBlind
) {
    // TODO: Add validation annotations (@NotBlank, @Min, @Max) once Jakarta Validation is configured
}
