package com.jkingai.pokertutor.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void placeBetDeductsChips() {
        Player player = new Player("Test", 1000);
        player.placeBet(200);
        assertEquals(800, player.getChips());
        assertEquals(200, player.getCurrentBet());
    }

    @Test
    void placeBetCappedAtChipStack() {
        Player player = new Player("Test", 100);
        player.placeBet(500);
        assertEquals(0, player.getChips());
        assertEquals(100, player.getCurrentBet());
    }

    @Test
    void foldSetsFlag() {
        Player player = new Player("Test", 1000);
        assertFalse(player.isFolded());
        player.fold();
        assertTrue(player.isFolded());
    }

    @Test
    void isAllInWhenNoChipsAndNotFolded() {
        Player player = new Player("Test", 100);
        player.placeBet(100);
        assertTrue(player.isAllIn());
    }

    @Test
    void notAllInWhenFolded() {
        Player player = new Player("Test", 100);
        player.placeBet(100);
        player.fold();
        assertFalse(player.isAllIn());
    }

    @Test
    void resetForNewHand() {
        Player player = new Player("Test", 1000);
        player.placeBet(200);
        player.fold();
        player.setHoleCards(List.of(
                new Card(Card.Rank.ACE, Card.Suit.SPADES),
                new Card(Card.Rank.KING, Card.Suit.SPADES)));
        player.resetForNewHand();

        assertEquals(0, player.getCurrentBet());
        assertFalse(player.isFolded());
        assertTrue(player.getHoleCards().isEmpty());
        assertEquals(800, player.getChips()); // chips unchanged
    }

    @Test
    void addChips() {
        Player player = new Player("Test", 1000);
        player.addChips(500);
        assertEquals(1500, player.getChips());
    }

    @Test
    void resetBet() {
        Player player = new Player("Test", 1000);
        player.placeBet(200);
        player.resetBet();
        assertEquals(0, player.getCurrentBet());
    }
}
