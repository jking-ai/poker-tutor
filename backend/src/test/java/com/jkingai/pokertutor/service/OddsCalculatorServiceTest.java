package com.jkingai.pokertutor.service;

import com.jkingai.pokertutor.model.Card;
import com.jkingai.pokertutor.model.Card.Rank;
import com.jkingai.pokertutor.model.Card.Suit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OddsCalculatorServiceTest {

    private OddsCalculatorService service;

    @BeforeEach
    void setUp() {
        service = new OddsCalculatorService(new HandEvaluatorService());
    }

    @Test
    void potOddsCalculation() {
        // Pot is 100, cost to call is 50 => need 50/(100+50) = 33% equity
        double odds = service.calculatePotOdds(100, 50);
        assertEquals(1.0 / 3, odds, 0.01);
    }

    @Test
    void potOddsZeroCostToCall() {
        assertEquals(0.0, service.calculatePotOdds(100, 0));
    }

    @Test
    void formatPotOddsRatio() {
        String ratio = service.formatPotOddsRatio(100, 50);
        assertEquals("2.0:1", ratio);
    }

    @Test
    void countOutsWithFlushDraw() {
        // 4 hearts in hand + community, need 1 more for flush
        List<Card> hole = List.of(
                new Card(Rank.ACE, Suit.HEARTS), new Card(Rank.KING, Suit.HEARTS));
        List<Card> community = List.of(
                new Card(Rank.SEVEN, Suit.HEARTS), new Card(Rank.TWO, Suit.HEARTS),
                new Card(Rank.TEN, Suit.CLUBS));

        OddsCalculatorService.OutsResult result = service.countOuts(hole, community);
        assertTrue(result.totalOuts() > 0, "Should have outs for flush");
    }

    @Test
    void countOutsPreFlop() {
        List<Card> hole = List.of(
                new Card(Rank.ACE, Suit.SPADES), new Card(Rank.KING, Suit.HEARTS));
        OddsCalculatorService.OutsResult result = service.countOuts(hole, List.of());
        assertEquals(0, result.totalOuts()); // No outs pre-flop (no community cards)
    }

    @Test
    void equityEstimateReasonableRange() {
        List<Card> hole = List.of(
                new Card(Rank.ACE, Suit.SPADES), new Card(Rank.ACE, Suit.HEARTS));
        List<Card> community = List.of(
                new Card(Rank.TWO, Suit.CLUBS), new Card(Rank.SEVEN, Suit.DIAMONDS),
                new Card(Rank.TEN, Suit.SPADES));

        OddsCalculatorService.EquityResult result = service.estimateEquity(hole, community, 500);

        // Pocket aces on a dry board should have high equity
        assertTrue(result.winProbability() > 0.6, "AA should have >60% equity: " + result.winProbability());
        assertTrue(result.winProbability() + result.tieProbability() + result.loseProbability() > 0.99);
    }

    @Test
    void equityEstimatePreFlop() {
        List<Card> hole = List.of(
                new Card(Rank.ACE, Suit.SPADES), new Card(Rank.ACE, Suit.HEARTS));

        OddsCalculatorService.EquityResult result = service.estimateEquity(hole, List.of(), 200);
        assertTrue(result.winProbability() > 0.7, "AA pre-flop should dominate");
    }
}
