package com.tetgame.modules.game.tienlen;

import java.util.List;
import java.util.UUID;

public class TurnManager {
    private final List<UUID> players;
    private int idx = 0;

    public TurnManager(List<UUID> players) { this.players = players; }
    public UUID current() { return players.get(idx); }
    public void advance() { idx = (idx + 1) % players.size(); }
}
