package com.jkingai.pokertutor.dto;

import com.jkingai.pokertutor.model.*;
import com.jkingai.pokertutor.model.Game.ActionRecord;

import java.util.List;

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
        int bigBlind,
        List<String> validActions,
        String winnerMessage,
        boolean gameOver,
        boolean aiEnabled,
        List<LogEntry> actionLog
) {

    public static GameResponse fromGame(Game game, List<PlayerAction> validActions, boolean aiEnabled) {
        boolean isShowdown = game.getPhase() == GamePhase.SHOWDOWN;

        List<PlayerDto> playerDtos = new java.util.ArrayList<>();
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player p = game.getPlayers().get(i);
            List<CardDto> holeCards;
            if (i == 0 || isShowdown) {
                // Show human's cards always; show opponent cards at showdown
                holeCards = p.getHoleCards().stream()
                        .map(c -> new CardDto(c.rank().name(), c.suit().name()))
                        .toList();
            } else {
                // Hide opponent cards
                holeCards = null;
            }
            playerDtos.add(new PlayerDto(p.getName(), p.getChips(), holeCards,
                    p.getCurrentBet(), p.isFolded(), p.isDealer()));
        }

        List<CardDto> communityCardDtos = game.getCommunityCards().stream()
                .map(c -> new CardDto(c.rank().name(), c.suit().name()))
                .toList();

        // Redact AI reasoning until the hand is over to avoid leaking hole card info
        String aiName = game.getAiPlayer().getName();
        boolean handOver = isShowdown || game.isGameOver();

        ActionDetail lastActionDto = null;
        ActionRecord last = game.getLastAction();
        if (last != null) {
            // During play: show tableTalk for AI; after hand: show full reasoning
            String displayReasoning = last.player().equals(aiName)
                    ? (handOver ? last.reasoning() : last.tableTalk())
                    : last.reasoning();
            lastActionDto = new ActionDetail(last.player(), last.action().name(),
                    last.amount(), displayReasoning);
        }

        List<String> validActionNames = validActions != null
                ? validActions.stream().map(PlayerAction::name).toList()
                : List.of();

        List<LogEntry> logEntries = game.getActionHistory().stream()
                .map(ar -> {
                    // During play: show tableTalk for AI; after hand: show full reasoning
                    String displayMsg;
                    if (ar.player().equals(aiName)) {
                        displayMsg = handOver ? ar.reasoning() : ar.tableTalk();
                    } else {
                        displayMsg = ar.reasoning();
                    }
                    return new LogEntry(ar.player(), ar.action() != null ? ar.action().name() : null,
                            ar.amount(), displayMsg, ar.phase() != null ? ar.phase().name() : null);
                })
                .toList();

        return new GameResponse(
                game.getGameId(), game.getPhase(), game.getPot(),
                communityCardDtos, playerDtos, game.getCurrentPlayerIndex(),
                game.getHandNumber(), lastActionDto, game.getSmallBlind(),
                game.getBigBlind(), validActionNames, game.getWinnerMessage(),
                game.isGameOver(), aiEnabled, logEntries
        );
    }

    public record CardDto(String rank, String suit) {}

    public record PlayerDto(
            String name, int chips, List<CardDto> holeCards,
            int currentBet, boolean folded, boolean isDealer
    ) {}

    public record ActionDetail(String player, String action, int amount, String reasoning) {}

    public record LogEntry(String player, String action, int amount, String message, String phase) {}
}
