package com.jkingai.pokertutor.dto;

import com.jkingai.pokertutor.model.HandRank;
import com.jkingai.pokertutor.model.PlayerAction;

/**
 * Response DTO for coaching advice, combining deterministic math with LLM explanations.
 */
public record CoachingResponse(
        String advice,
        PlayerAction recommendedAction,
        String confidence,
        HandStrengthDto handStrength,
        OddsDto odds,
        ExplanationDto explanation
) {
    public record HandStrengthDto(
            HandRank currentRank,
            String bestHand,
            int nutDistance
    ) {}

    public record OddsDto(
            double potOdds,
            String potOddsRatio,
            double handEquity,
            int outs,
            String outsDescription
    ) {}

    public record ExplanationDto(
            String situation,
            String mathSummary,
            String recommendation
    ) {}
}
