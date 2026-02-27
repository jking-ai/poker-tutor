package com.jkingai.pokertutor.service;

import com.jkingai.pokertutor.dto.CoachingResponse;
import com.jkingai.pokertutor.model.Game;
import org.springframework.stereotype.Service;

/**
 * LLM-powered coaching agent that provides real-time poker strategy advice.
 * Uses Spring AI ChatClient with the coach persona prompt.
 * Receives pre-calculated mathematical data and generates plain-language explanations.
 */
@Service
public class CoachAgentService {

    // TODO: Inject ChatClient (coach-specific bean from VertexAiConfig)
    // TODO: Inject OddsCalculatorService, HandEvaluatorService

    /**
     * Generate coaching advice for the player's current hand.
     * Combines deterministic math (pot odds, equity) with LLM-generated explanations.
     */
    public CoachingResponse getCoachingAdvice(Game game) {
        // TODO: Calculate pot odds via OddsCalculatorService
        // TODO: Evaluate hand strength via HandEvaluatorService
        // TODO: Count outs and estimate equity
        // TODO: Identify the nut hand
        // TODO: Build prompt context with all mathematical data:
        //   - "Pot odds: 30% (2.3:1)"
        //   - "Hand equity: 65%"
        //   - "Outs: 9 (flush draw)"
        //   - "Current hand: pair of Kings"
        //   - "The nut: Ace-high flush"
        // TODO: Call ChatClient.prompt() with math context + game state
        // TODO: Parse response into CoachingResponse DTO
        // TODO: Return combined response with both math data and LLM explanation
        throw new UnsupportedOperationException("TODO: Implement getCoachingAdvice");
    }
}
