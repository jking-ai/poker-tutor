package com.jkingai.pokertutor.controller;

import com.jkingai.pokertutor.dto.CoachingResponse;
import com.jkingai.pokertutor.dto.OddsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for coaching and odds endpoints.
 * These are read-only endpoints that do not modify game state.
 */
@RestController
@RequestMapping("/api/v1/games/{id}")
public class CoachingController {

    // TODO: Inject CoachAgentService, OddsCalculatorService, GameService

    /**
     * GET /api/v1/games/{id}/coaching -- Get coaching advice for the current hand.
     * Combines deterministic math (odds, equity) with LLM-generated explanations.
     */
    @GetMapping("/coaching")
    public ResponseEntity<CoachingResponse> getCoaching(@PathVariable String id) {
        // TODO: Get current game state from GameService
        // TODO: Calculate odds via OddsCalculatorService
        // TODO: Evaluate hand via HandEvaluatorService
        // TODO: Pass math results to CoachAgentService for plain-language advice
        // TODO: Return combined CoachingResponse
        throw new UnsupportedOperationException("TODO: Implement getCoaching");
    }

    /**
     * GET /api/v1/games/{id}/odds -- Get raw pot odds and probabilities.
     * Pure mathematical calculation, no LLM involved.
     */
    @GetMapping("/odds")
    public ResponseEntity<OddsResponse> getOdds(@PathVariable String id) {
        // TODO: Get current game state from GameService
        // TODO: Calculate pot odds, equity, outs via OddsCalculatorService
        // TODO: Identify the nut hand
        // TODO: Return OddsResponse
        throw new UnsupportedOperationException("TODO: Implement getOdds");
    }
}
