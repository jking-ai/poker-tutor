package com.jkingai.pokertutor.exception;

/**
 * Thrown when a game ID is not found in the game store.
 * Maps to HTTP 404.
 */
public class GameNotFoundException extends RuntimeException {

    private final String gameId;

    public GameNotFoundException(String gameId) {
        super("No game found with ID '" + gameId + "'.");
        this.gameId = gameId;
    }

    public String getGameId() {
        return gameId;
    }
}
