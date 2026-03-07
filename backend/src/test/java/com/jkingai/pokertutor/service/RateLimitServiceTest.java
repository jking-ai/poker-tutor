package com.jkingai.pokertutor.service;

import com.jkingai.pokertutor.exception.RateLimitException;
import com.jkingai.pokertutor.model.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService(5, 3, 2);
    }

    @Test
    void gameCreationAllowedWhenUnderLimit() {
        assertDoesNotThrow(() -> rateLimitService.checkGameCreationAllowed(2));
    }

    @Test
    void gameCreationBlockedWhenAtLimit() {
        RateLimitException ex = assertThrows(RateLimitException.class,
                () -> rateLimitService.checkGameCreationAllowed(3));
        assertEquals("GAME_LIMIT", ex.getCode());
    }

    @Test
    void aiCallAllowedWhenUnderLimit() {
        Game game = new Game();
        assertTrue(rateLimitService.isAiCallAllowed(game));
    }

    @Test
    void aiCallBlockedWhenAtLimit() {
        Game game = new Game();
        for (int i = 0; i < 5; i++) {
            game.incrementAiCallCount();
        }
        assertFalse(rateLimitService.isAiCallAllowed(game));
    }

    @Test
    void coachingAllowedWhenUnderLimit() {
        Game game = new Game();
        assertDoesNotThrow(() -> rateLimitService.checkCoachingAllowed(game));
    }

    @Test
    void coachingBlockedWhenAtLimit() {
        Game game = new Game();
        game.incrementCoachingCallsThisHand();
        game.incrementCoachingCallsThisHand();
        RateLimitException ex = assertThrows(RateLimitException.class,
                () -> rateLimitService.checkCoachingAllowed(game));
        assertEquals("COACHING_LIMIT", ex.getCode());
    }

    @Test
    void coachingCounterResetsForNewHand() {
        Game game = new Game();
        game.incrementCoachingCallsThisHand();
        game.incrementCoachingCallsThisHand();
        game.resetCoachingCallsThisHand();
        assertDoesNotThrow(() -> rateLimitService.checkCoachingAllowed(game));
        assertEquals(0, game.getCoachingCallsThisHand());
    }
}
