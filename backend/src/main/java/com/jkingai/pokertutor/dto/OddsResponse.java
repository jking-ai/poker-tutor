package com.jkingai.pokertutor.dto;

import java.util.List;

/**
 * Response DTO for raw odds and probability data (no coaching narrative).
 */
public record OddsResponse(
        int potSize,
        int costToCall,
        double potOdds,
        String potOddsRatio,
        double handEquity,
        int outs,
        List<OutDetail> outsDetails,
        double winProbability,
        double tieProbability,
        double loseProbability,
        boolean isNut,
        String nutHand
) {
    public record OutDetail(
            String card,
            int count,
            String description
    ) {}
}
