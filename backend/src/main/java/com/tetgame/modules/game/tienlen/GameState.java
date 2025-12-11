package com.tetgame.modules.game.tienlen;

import java.util.*;

public class GameState {
    private final UUID roomId;
    private final Map<UUID, List<Card>> hands = new HashMap<>();
    private final Map<UUID, Boolean> finished = new HashMap<>();
    private final List<UUID> turnOrder;
    private int currentTurnIndex = 0;
    
    // Hiện tại lượt đánh: bài nào được đánh lên bàn (pile)
    private Play currentPile = null;
    // Người nào đã pass trong lượt hiện tại
    private Set<UUID> passedThisTurn = new HashSet<>();
    // Log từng lượt đánh: (player, cards, timestamp)
    private List<java.util.Map<String, Object>> gameLog = new ArrayList<>();

    public GameState(UUID roomId, List<UUID> turnOrder) {
        this.roomId = roomId;
        this.turnOrder = turnOrder;
    }

    public void setHand(UUID playerId, List<Card> hand) { hands.put(playerId, hand); }
    public List<Card> getHand(UUID playerId) { return hands.get(playerId); }
    public UUID getCurrentPlayer() { return turnOrder.get(currentTurnIndex); }
    public void nextTurn() { currentTurnIndex = (currentTurnIndex + 1) % turnOrder.size(); }
    public Map<UUID, List<Card>> getHands() { return hands; }
    
    public Play getCurrentPile() { return currentPile; }
    public void setCurrentPile(Play pile) { this.currentPile = pile; }
    public void clearCurrentPile() { this.currentPile = null; }
    
    public void markPass(UUID playerId) { passedThisTurn.add(playerId); }
    public Set<UUID> getPassedThisTurn() { return passedThisTurn; }
    public void resetPassedThisTurn() { passedThisTurn.clear(); }
    public boolean areAllOthersPass(UUID currentPlayer) {
        return passedThisTurn.size() == turnOrder.size() - 1 && !passedThisTurn.contains(currentPlayer);
    }
    
    public void logMove(UUID playerId, List<Card> cards) {
        var entry = new HashMap<String, Object>();
        entry.put("playerId", playerId.toString());
        entry.put("cards", cards.stream().map(Card::toString).toList());
        entry.put("timestamp", System.currentTimeMillis());
        gameLog.add(entry);
    }
    public List<java.util.Map<String, Object>> getGameLog() { return gameLog; }
    
    public UUID getRoomId() { return roomId; }
    public List<UUID> getTurnOrder() { return turnOrder; }
    public int getCurrentTurnIndex() { return currentTurnIndex; }
    public void setCurrentTurnIndex(int idx) { this.currentTurnIndex = idx; }
}
