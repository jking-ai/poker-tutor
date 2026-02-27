package com.jkingai.pokertutor.dto;

import com.jkingai.pokertutor.model.GamePhase;

import java.util.List;

/**
 * Response DTO representing the current game state.
 * Opponent's hole cards are null unless phase is SHOWDOWN.
 */
public record GameResponse(
        String gameId,
        GamePhase phase,
        int pot,
        List<CardDto> communityCards,
        List<PlayerDto> players,
        int currentPlayerIndex,
        int handNumber,
        ActionDetail lastAction,
        int smallBlind,
        int bigBlind
) {
    // TODO: Add static factory method to convert from Game model to GameResponse DTO
    //       (hiding opponent's hole cards when not at SHOWDOWN)

    public record CardDto(String rank, String suit) {}

    public record PlayerDto(
            String name,
            int chips,
            List<CardDto> holeCards,
            int currentBet,
            boolean folded,
            boolean isDealer
    ) {}

    public record ActionDetail(
            String player,
            String action,
            int amount,
            String reasoning
    ) {}
}
