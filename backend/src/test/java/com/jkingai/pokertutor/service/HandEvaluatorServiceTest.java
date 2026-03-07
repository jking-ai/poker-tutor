package com.jkingai.pokertutor.service;

import com.jkingai.pokertutor.model.Card;
import com.jkingai.pokertutor.model.Card.Rank;
import com.jkingai.pokertutor.model.Card.Suit;
import com.jkingai.pokertutor.model.HandRank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HandEvaluatorServiceTest {

    private HandEvaluatorService service;

    @BeforeEach
    void setUp() {
        service = new HandEvaluatorService();
    }

    @Test
    void royalFlush() {
        List<Card> cards = List.of(
                new Card(Rank.ACE, Suit.SPADES), new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.QUEEN, Suit.SPADES), new Card(Rank.JACK, Suit.SPADES),
                new Card(Rank.TEN, Suit.SPADES));
        assertEquals(HandRank.ROYAL_FLUSH, service.evaluateHand(cards));
    }

    @Test
    void straightFlush() {
        List<Card> cards = List.of(
                new Card(Rank.NINE, Suit.HEARTS), new Card(Rank.EIGHT, Suit.HEARTS),
                new Card(Rank.SEVEN, Suit.HEARTS), new Card(Rank.SIX, Suit.HEARTS),
                new Card(Rank.FIVE, Suit.HEARTS));
        assertEquals(HandRank.STRAIGHT_FLUSH, service.evaluateHand(cards));
    }

    @Test
    void fourOfAKind() {
        List<Card> cards = List.of(
                new Card(Rank.ACE, Suit.SPADES), new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.ACE, Suit.DIAMONDS), new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.KING, Suit.SPADES));
        assertEquals(HandRank.FOUR_OF_A_KIND, service.evaluateHand(cards));
    }

    @Test
    void fullHouse() {
        List<Card> cards = List.of(
                new Card(Rank.KING, Suit.SPADES), new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.KING, Suit.DIAMONDS), new Card(Rank.QUEEN, Suit.CLUBS),
                new Card(Rank.QUEEN, Suit.SPADES));
        assertEquals(HandRank.FULL_HOUSE, service.evaluateHand(cards));
    }

    @Test
    void flush() {
        List<Card> cards = List.of(
                new Card(Rank.ACE, Suit.CLUBS), new Card(Rank.TEN, Suit.CLUBS),
                new Card(Rank.EIGHT, Suit.CLUBS), new Card(Rank.SIX, Suit.CLUBS),
                new Card(Rank.THREE, Suit.CLUBS));
        assertEquals(HandRank.FLUSH, service.evaluateHand(cards));
    }

    @Test
    void straight() {
        List<Card> cards = List.of(
                new Card(Rank.NINE, Suit.SPADES), new Card(Rank.EIGHT, Suit.HEARTS),
                new Card(Rank.SEVEN, Suit.DIAMONDS), new Card(Rank.SIX, Suit.CLUBS),
                new Card(Rank.FIVE, Suit.SPADES));
        assertEquals(HandRank.STRAIGHT, service.evaluateHand(cards));
    }

    @Test
    void aceLowStraight() {
        List<Card> cards = List.of(
                new Card(Rank.ACE, Suit.SPADES), new Card(Rank.TWO, Suit.HEARTS),
                new Card(Rank.THREE, Suit.DIAMONDS), new Card(Rank.FOUR, Suit.CLUBS),
                new Card(Rank.FIVE, Suit.SPADES));
        assertEquals(HandRank.STRAIGHT, service.evaluateHand(cards));
    }

    @Test
    void threeOfAKind() {
        List<Card> cards = List.of(
                new Card(Rank.JACK, Suit.SPADES), new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.JACK, Suit.DIAMONDS), new Card(Rank.NINE, Suit.CLUBS),
                new Card(Rank.SEVEN, Suit.SPADES));
        assertEquals(HandRank.THREE_OF_A_KIND, service.evaluateHand(cards));
    }

    @Test
    void twoPair() {
        List<Card> cards = List.of(
                new Card(Rank.KING, Suit.SPADES), new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.NINE, Suit.DIAMONDS), new Card(Rank.NINE, Suit.CLUBS),
                new Card(Rank.FIVE, Suit.SPADES));
        assertEquals(HandRank.TWO_PAIR, service.evaluateHand(cards));
    }

    @Test
    void pair() {
        List<Card> cards = List.of(
                new Card(Rank.ACE, Suit.SPADES), new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.KING, Suit.DIAMONDS), new Card(Rank.QUEEN, Suit.CLUBS),
                new Card(Rank.JACK, Suit.SPADES));
        assertEquals(HandRank.PAIR, service.evaluateHand(cards));
    }

    @Test
    void highCard() {
        List<Card> cards = List.of(
                new Card(Rank.ACE, Suit.SPADES), new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.NINE, Suit.DIAMONDS), new Card(Rank.SEVEN, Suit.CLUBS),
                new Card(Rank.THREE, Suit.SPADES));
        assertEquals(HandRank.HIGH_CARD, service.evaluateHand(cards));
    }

    @Test
    void bestOfSevenFindsFlush() {
        List<Card> hole = List.of(
                new Card(Rank.ACE, Suit.HEARTS), new Card(Rank.KING, Suit.HEARTS));
        List<Card> community = List.of(
                new Card(Rank.QUEEN, Suit.HEARTS), new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.NINE, Suit.HEARTS), new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.DIAMONDS));
        HandEvaluatorService.HandEvaluation eval = service.evaluateBestHand(hole, community);
        assertEquals(HandRank.FLUSH, eval.rank());
    }

    @Test
    void bestOfSevenFindsStraight() {
        List<Card> hole = List.of(
                new Card(Rank.TEN, Suit.SPADES), new Card(Rank.NINE, Suit.HEARTS));
        List<Card> community = List.of(
                new Card(Rank.EIGHT, Suit.DIAMONDS), new Card(Rank.SEVEN, Suit.CLUBS),
                new Card(Rank.SIX, Suit.SPADES), new Card(Rank.TWO, Suit.HEARTS),
                new Card(Rank.THREE, Suit.DIAMONDS));
        HandEvaluatorService.HandEvaluation eval = service.evaluateBestHand(hole, community);
        assertEquals(HandRank.STRAIGHT, eval.rank());
    }

    @Test
    void compareHandsHigherRankWins() {
        List<Card> hand1 = List.of(
                new Card(Rank.ACE, Suit.SPADES), new Card(Rank.ACE, Suit.HEARTS));
        List<Card> hand2 = List.of(
                new Card(Rank.KING, Suit.SPADES), new Card(Rank.QUEEN, Suit.HEARTS));
        List<Card> community = List.of(
                new Card(Rank.ACE, Suit.DIAMONDS), new Card(Rank.TEN, Suit.CLUBS),
                new Card(Rank.SEVEN, Suit.SPADES), new Card(Rank.FOUR, Suit.HEARTS),
                new Card(Rank.TWO, Suit.DIAMONDS));
        assertTrue(service.compareHands(hand1, hand2, community) > 0);
    }

    @Test
    void compareHandsKickerBreaksTie() {
        List<Card> hand1 = List.of(
                new Card(Rank.ACE, Suit.SPADES), new Card(Rank.KING, Suit.HEARTS));
        List<Card> hand2 = List.of(
                new Card(Rank.ACE, Suit.DIAMONDS), new Card(Rank.QUEEN, Suit.CLUBS));
        List<Card> community = List.of(
                new Card(Rank.ACE, Suit.HEARTS), new Card(Rank.TEN, Suit.CLUBS),
                new Card(Rank.SEVEN, Suit.SPADES), new Card(Rank.FOUR, Suit.HEARTS),
                new Card(Rank.TWO, Suit.DIAMONDS));
        assertTrue(service.compareHands(hand1, hand2, community) > 0);
    }

    @Test
    void compareHandsTie() {
        List<Card> hand1 = List.of(
                new Card(Rank.TWO, Suit.SPADES), new Card(Rank.THREE, Suit.HEARTS));
        List<Card> hand2 = List.of(
                new Card(Rank.TWO, Suit.DIAMONDS), new Card(Rank.THREE, Suit.CLUBS));
        List<Card> community = List.of(
                new Card(Rank.ACE, Suit.HEARTS), new Card(Rank.KING, Suit.CLUBS),
                new Card(Rank.QUEEN, Suit.SPADES), new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.TEN, Suit.DIAMONDS));
        // Both have A-K-Q-J-T straight from community
        assertEquals(0, service.compareHands(hand1, hand2, community));
    }

    @Test
    void aceLowStraightLosesToHigherStraight() {
        List<Card> hand1 = List.of(
                new Card(Rank.ACE, Suit.SPADES), new Card(Rank.TWO, Suit.HEARTS));
        List<Card> hand2 = List.of(
                new Card(Rank.SIX, Suit.DIAMONDS), new Card(Rank.SEVEN, Suit.CLUBS));
        List<Card> community = List.of(
                new Card(Rank.THREE, Suit.HEARTS), new Card(Rank.FOUR, Suit.CLUBS),
                new Card(Rank.FIVE, Suit.SPADES), new Card(Rank.NINE, Suit.HEARTS),
                new Card(Rank.JACK, Suit.DIAMONDS));
        // hand1 has A-2-3-4-5 (high=5), hand2 has 3-4-5-6-7 (high=7)
        assertTrue(service.compareHands(hand1, hand2, community) < 0);
    }
}
