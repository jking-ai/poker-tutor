package com.jkingai.pokertutor.service;

import com.jkingai.pokertutor.model.Game;
import com.jkingai.pokertutor.model.PlayerAction;
import org.springframework.stereotype.Service;

/**
 * LLM-powered opponent agent that plays poker against the user.
 * Uses Spring AI ChatClient with the opponent persona prompt.
 * Makes contextually appropriate decisions including occasional bluffs.
 */
@Service
public class OpponentAgentService {

    // TODO: Inject ChatClient (opponent-specific bean from VertexAiConfig)

    /**
     * Determine the opponent's action for the current game state.
     * Sends game context to the LLM and parses the structured response.
     */
    public OpponentDecision getOpponentAction(Game game) {
        // TODO: Build prompt context from game state:
        //   - Current phase, pot size, community cards
        //   - Opponent's hole cards (the AI can see its own cards)
        //   - Action history for this hand
        //   - Player's current bet, chip stacks
        // TODO: Call ChatClient.prompt() with game context
        // TODO: Parse structured response into action + amount + reasoning
        // TODO: Validate the action is legal for the current state
        // TODO: Fallback to CHECK or CALL if LLM response is invalid or call fails
        throw new UnsupportedOperationException("TODO: Implement getOpponentAction");
    }

    /**
     * Inner record for the opponent's decision.
     */
    public record OpponentDecision(
            PlayerAction action,
            int amount,
            String reasoning
    ) {}
}
