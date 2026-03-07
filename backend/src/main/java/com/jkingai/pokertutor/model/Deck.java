package com.jkingai.pokertutor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    private final List<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
        initializeDeck();
    }

    private void initializeDeck() {
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card deal() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Cannot deal from an empty deck");
        }
        return cards.removeFirst();
    }

    public List<Card> deal(int count) {
        if (count > cards.size()) {
            throw new IllegalStateException("Not enough cards in deck. Requested: " + count + ", remaining: " + cards.size());
        }
        List<Card> dealt = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            dealt.add(cards.removeFirst());
        }
        return dealt;
    }

    public void reset() {
        cards.clear();
        initializeDeck();
    }

    public int remaining() {
        return cards.size();
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }
}
