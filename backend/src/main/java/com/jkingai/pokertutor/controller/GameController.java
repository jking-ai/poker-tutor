package com.jkingai.pokertutor.controller;

import com.jkingai.pokertutor.dto.ActionRequest;
import com.jkingai.pokertutor.dto.GameRequest;
import com.jkingai.pokertutor.dto.GameResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for game management endpoints.
 * Handles game creation, state retrieval, player actions, hand history, and dealing new hands.
 */
@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    // TODO: Inject GameService

    /**
     * POST /api/v1/games -- Start a new heads-up Texas Hold'em game.
     */
    @PostMapping
    public ResponseEntity<GameResponse> createGame(@RequestBody GameRequest request) {
        // TODO: Validate request
        // TODO: Call gameService.createGame()
        // TODO: Return 201 Created with game state
        throw new UnsupportedOperationException("TODO: Implement createGame");
    }

    /**
     * GET /api/v1/games/{id} -- Get current game state.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> getGame(@PathVariable String id) {
        // TODO: Call gameService.getGame(id)
        // TODO: Return game state with opponent cards hidden (unless SHOWDOWN)
        throw new UnsupportedOperationException("TODO: Implement getGame");
    }

    /**
     * POST /api/v1/games/{id}/actions -- Submit a player action.
     * After processing the player's action, automatically processes the opponent's turn.
     */
    @PostMapping("/{id}/actions")
    public ResponseEntity<GameResponse> submitAction(
            @PathVariable String id,
            @RequestBody ActionRequest request) {
        // TODO: Validate action against current game state
        // TODO: Call gameService.processAction(id, request)
        // TODO: If opponent's turn, call opponentAgentService for opponent action
        // TODO: Write updated state to Firebase RTDB
        // TODO: Return updated game state
        throw new UnsupportedOperationException("TODO: Implement submitAction");
    }

    /**
     * GET /api/v1/games/{id}/history -- Get hand history for the game.
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<?> getHistory(@PathVariable String id) {
        // TODO: Call gameService.getHistory(id)
        // TODO: Return hand history
        throw new UnsupportedOperationException("TODO: Implement getHistory");
    }

    /**
     * POST /api/v1/games/{id}/next-hand -- Deal the next hand.
     */
    @PostMapping("/{id}/next-hand")
    public ResponseEntity<GameResponse> nextHand(@PathVariable String id) {
        // TODO: Call gameService.dealNextHand(id)
        // TODO: Write updated state to Firebase RTDB
        // TODO: Return new hand state
        throw new UnsupportedOperationException("TODO: Implement nextHand");
    }
}
