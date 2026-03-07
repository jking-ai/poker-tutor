package com.jkingai.pokertutor.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @Test
    void newDeckHas52Cards() {
        Deck deck = new Deck();
        assertEquals(52, deck.remaining());
    }

    @Test
    void newDeckHasAllUniqueCards() {
        Deck deck = new Deck();
        Set<Card> cardSet = new HashSet<>(deck.getCards());
        assertEquals(52, cardSet.size());
    }

    @Test
    void dealRemovesOneCard() {
        Deck deck = new Deck();
        deck.shuffle();
        Card card = deck.deal();
        assertNotNull(card);
        assertEquals(51, deck.remaining());
    }

    @Test
    void dealMultipleCards() {
        Deck deck = new Deck();
        deck.shuffle();
        List<Card> cards = deck.deal(5);
        assertEquals(5, cards.size());
        assertEquals(47, deck.remaining());
    }

    @Test
    void dealFromEmptyDeckThrows() {
        Deck deck = new Deck();
        deck.deal(52);
        assertThrows(IllegalStateException.class, deck::deal);
    }

    @Test
    void resetRestoresDeck() {
        Deck deck = new Deck();
        deck.shuffle();
        deck.deal(10);
        assertEquals(42, deck.remaining());
        deck.reset();
        assertEquals(52, deck.remaining());
    }

    @Test
    void shuffleChangesOrder() {
        Deck deck1 = new Deck();
        List<Card> original = List.copyOf(deck1.getCards());
        deck1.shuffle();
        // Extremely unlikely to remain identical after shuffle
        assertNotEquals(original, deck1.getCards());
    }
}
