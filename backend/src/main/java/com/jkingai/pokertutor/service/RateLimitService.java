package com.jkingai.pokertutor.service;

import com.jkingai.pokertutor.exception.RateLimitException;
import com.jkingai.pokertutor.model.Game;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final int maxAiCallsPerGame;
    private final int maxConcurrentGames;
    private final int maxCoachingPerHand;

    public RateLimitService(
            @Value("${app.limits.max-ai-calls-per-game:200}") int maxAiCallsPerGame,
            @Value("${app.limits.max-concurrent-games:15}") int maxConcurrentGames,
            @Value("${app.limits.max-coaching-per-hand:3}") int maxCoachingPerHand) {
        this.maxAiCallsPerGame = maxAiCallsPerGame;
        this.maxConcurrentGames = maxConcurrentGames;
        this.maxCoachingPerHand = maxCoachingPerHand;
    }

    public void checkGameCreationAllowed(int currentGameCount) {
        if (currentGameCount >= maxConcurrentGames) {
            throw new RateLimitException("GAME_LIMIT",
                    "Maximum concurrent games reached (" + maxConcurrentGames + "). Please wait for existing games to finish.");
        }
    }

    public boolean isAiCallAllowed(Game game) {
        return game.getAiCallCount() < maxAiCallsPerGame;
    }

    public void checkCoachingAllowed(Game game) {
        if (game.getCoachingCallsThisHand() >= maxCoachingPerHand) {
            throw new RateLimitException("COACHING_LIMIT",
                    "Coaching limit reached (" + maxCoachingPerHand + " per hand). Wait for the next hand.");
        }
    }

    public int getMaxConcurrentGames() {
        return maxConcurrentGames;
    }
}
