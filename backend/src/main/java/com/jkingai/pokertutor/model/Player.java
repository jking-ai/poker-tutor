package com.jkingai.pokertutor.model;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private String name;
    private int chips;
    private List<Card> holeCards;
    private int currentBet;
    private boolean folded;
    private boolean isDealer;

    public Player(String name, int chips) {
        this.name = name;
        this.chips = chips;
        this.holeCards = new ArrayList<>();
        this.currentBet = 0;
        this.folded = false;
        this.isDealer = false;
    }

    public void placeBet(int amount) {
        int actual = Math.min(amount, chips);
        chips -= actual;
        currentBet += actual;
    }

    public void resetBet() {
        currentBet = 0;
    }

    public void addChips(int amount) {
        chips += amount;
    }

    public void fold() {
        folded = true;
    }

    public boolean isAllIn() {
        return chips == 0 && !folded;
    }

    public void resetForNewHand() {
        holeCards = new ArrayList<>();
        currentBet = 0;
        folded = false;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getChips() { return chips; }
    public void setChips(int chips) { this.chips = chips; }
    public List<Card> getHoleCards() { return holeCards; }
    public void setHoleCards(List<Card> holeCards) { this.holeCards = new ArrayList<>(holeCards); }
    public int getCurrentBet() { return currentBet; }
    public void setCurrentBet(int currentBet) { this.currentBet = currentBet; }
    public boolean isFolded() { return folded; }
    public void setFolded(boolean folded) { this.folded = folded; }
    public boolean isDealer() { return isDealer; }
    public void setDealer(boolean dealer) { isDealer = dealer; }
}
