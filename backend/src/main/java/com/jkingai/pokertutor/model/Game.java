package com.jkingai.pokertutor.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Game {

    private String gameId;
    private List<Player> players; // index 0 = human, index 1 = AI opponent
    private Deck deck;
    private List<Card> communityCards;
    private int pot;
    private GamePhase phase;
    private int currentPlayerIndex;
    private int dealerIndex;
    private int handNumber;
    private int smallBlind;
    private int bigBlind;
    private List<ActionRecord> actionHistory;
    private ActionRecord lastAction;
    private Set<Integer> playersActedThisRound;
    private String winnerMessage;
    private boolean gameOver;
    private int aiCallCount;
    private int coachingCallsThisHand;
    private Instant createdAt;

    public Game() {
        this.communityCards = new ArrayList<>();
        this.actionHistory = new ArrayList<>();
        this.playersActedThisRound = new HashSet<>();
        this.createdAt = Instant.now();
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public Player getOpponent() {
        return players.get(1 - currentPlayerIndex);
    }

    public Player getHumanPlayer() {
        return players.get(0);
    }

    public Player getAiPlayer() {
        return players.get(1);
    }

    public Player getDealer() {
        return players.get(dealerIndex);
    }

    public boolean isPlayerTurn(int playerIndex) {
        return currentPlayerIndex == playerIndex;
    }

    public boolean isHumanTurn() {
        return currentPlayerIndex == 0;
    }

    public void addToPot(int amount) {
        pot += amount;
    }

    public void advancePhase() {
        phase = switch (phase) {
            case PRE_FLOP -> GamePhase.FLOP;
            case FLOP -> GamePhase.TURN;
            case TURN -> GamePhase.RIVER;
            case RIVER -> GamePhase.SHOWDOWN;
            case SHOWDOWN -> GamePhase.SHOWDOWN;
        };
    }

    // Getters and setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }
    public Deck getDeck() { return deck; }
    public void setDeck(Deck deck) { this.deck = deck; }
    public List<Card> getCommunityCards() { return communityCards; }
    public void setCommunityCards(List<Card> communityCards) { this.communityCards = communityCards; }
    public int getPot() { return pot; }
    public void setPot(int pot) { this.pot = pot; }
    public GamePhase getPhase() { return phase; }
    public void setPhase(GamePhase phase) { this.phase = phase; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public void setCurrentPlayerIndex(int currentPlayerIndex) { this.currentPlayerIndex = currentPlayerIndex; }
    public int getDealerIndex() { return dealerIndex; }
    public void setDealerIndex(int dealerIndex) { this.dealerIndex = dealerIndex; }
    public int getHandNumber() { return handNumber; }
    public void setHandNumber(int handNumber) { this.handNumber = handNumber; }
    public int getSmallBlind() { return smallBlind; }
    public void setSmallBlind(int smallBlind) { this.smallBlind = smallBlind; }
    public int getBigBlind() { return bigBlind; }
    public void setBigBlind(int bigBlind) { this.bigBlind = bigBlind; }
    public List<ActionRecord> getActionHistory() { return actionHistory; }
    public void setActionHistory(List<ActionRecord> actionHistory) { this.actionHistory = actionHistory; }
    public ActionRecord getLastAction() { return lastAction; }
    public void setLastAction(ActionRecord lastAction) { this.lastAction = lastAction; }
    public Set<Integer> getPlayersActedThisRound() { return playersActedThisRound; }
    public void setPlayersActedThisRound(Set<Integer> playersActedThisRound) { this.playersActedThisRound = playersActedThisRound; }
    public String getWinnerMessage() { return winnerMessage; }
    public void setWinnerMessage(String winnerMessage) { this.winnerMessage = winnerMessage; }
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public int getAiCallCount() { return aiCallCount; }
    public void incrementAiCallCount() { this.aiCallCount++; }
    public int getCoachingCallsThisHand() { return coachingCallsThisHand; }
    public void incrementCoachingCallsThisHand() { this.coachingCallsThisHand++; }
    public void resetCoachingCallsThisHand() { this.coachingCallsThisHand = 0; }
    public Instant getCreatedAt() { return createdAt; }

    public record ActionRecord(String player, PlayerAction action, int amount, String reasoning, String tableTalk, GamePhase phase) {}

    public void addLogEntry(String message) {
        actionHistory.add(new ActionRecord("System", null, 0, message, null, phase));
    }

}
