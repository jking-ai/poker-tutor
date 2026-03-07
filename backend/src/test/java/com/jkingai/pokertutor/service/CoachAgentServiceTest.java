package com.jkingai.pokertutor.service;

import com.jkingai.pokertutor.dto.CoachingResponse;
import com.jkingai.pokertutor.dto.GameRequest;
import com.jkingai.pokertutor.model.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoachAgentServiceTest {

    private CoachAgentService coachAgentService;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        HandEvaluatorService handEvaluatorService = new HandEvaluatorService();
        OddsCalculatorService oddsCalculatorService = new OddsCalculatorService(handEvaluatorService);
        RateLimitService rateLimitService = new RateLimitService(200, 15, 3);
        coachAgentService = new CoachAgentService(oddsCalculatorService, handEvaluatorService, null, rateLimitService);
        gameService = new GameService(handEvaluatorService, new OpponentAgentService(null), rateLimitService);
    }

    @Test
    void coachingResponseHasAllFields() {
        Game game = gameService.createGame(new GameRequest("TestPlayer", 1000, 5, 10));
        CoachingResponse response = coachAgentService.getCoachingAdvice(game);

        assertNotNull(response.advice());
        assertNotNull(response.recommendedAction());
        assertNotNull(response.confidence());
        assertNotNull(response.handStrength());
        assertNotNull(response.odds());
        assertNotNull(response.explanation());
    }

    @Test
    void coachingConfidenceIsValid() {
        Game game = gameService.createGame(new GameRequest("TestPlayer", 1000, 5, 10));
        CoachingResponse response = coachAgentService.getCoachingAdvice(game);

        assertTrue(response.confidence().equals("HIGH") ||
                response.confidence().equals("MEDIUM") ||
                response.confidence().equals("LOW"));
    }

    @Test
    void coachingContainsMathData() {
        Game game = gameService.createGame(new GameRequest("TestPlayer", 1000, 5, 10));
        CoachingResponse response = coachAgentService.getCoachingAdvice(game);

        assertNotNull(response.odds().potOddsRatio());
        assertTrue(response.odds().handEquity() >= 0 && response.odds().handEquity() <= 1);
    }
}
