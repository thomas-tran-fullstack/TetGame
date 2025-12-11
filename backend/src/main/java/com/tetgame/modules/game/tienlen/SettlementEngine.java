package com.tetgame.modules.game.tienlen;

import java.util.Map;
import java.util.UUID;

public class SettlementEngine {
    /**
     * Simple settlement logic:
     * - If there is one or more winners (e.g., tới trắng), each winner gets +100, each loser -100.
     * - Returns map userId -> delta (positive = credit, negative = debit)
     */
    public static java.util.Map<UUID, Integer> settle(GameState state, com.tetgame.modules.room.entity.BetLevel betLevel) {
        var players = state.getHands().keySet().stream().toList();
        if (players.isEmpty()) return java.util.Collections.emptyMap();

        // Determine payout mapping by bet level
        long first, second, third, fourth;
        switch (betLevel) {
            case BAN2 -> {
                first = 20_000L; second = 10_000L; third = -10_000L; fourth = -20_000L;
            }
            case BAN3 -> {
                first = 100_000L; second = 50_000L; third = -50_000L; fourth = -100_000L;
            }
            case BAN4 -> {
                first = 200_000L; second = 100_000L; third = -100_000L; fourth = -200_000L;
            }
            case BAN5 -> {
                first = 1_000_000L; second = 500_000L; third = -500_000L; fourth = -1_000_000L;
            }
            default -> {
                first = 10_000L; second = 5_000L; third = -5_000L; fourth = -10_000L;
            }
        }

        // Rank players by remaining cards (fewer cards = higher rank)
        var ranking = state.getHands().entrySet().stream()
            .sorted(java.util.Map.Entry.comparingByValue((a, b) -> Integer.compare(a.size(), b.size())))
            .map(java.util.Map.Entry::getKey)
            .toList();

        java.util.Map<UUID, Integer> deltas = new java.util.HashMap<>();
        for (int i = 0; i < ranking.size(); i++) {
            UUID pid = ranking.get(i);
            long delta = 0L;
            if (i == 0) delta = first;
            else if (i == 1) delta = second;
            else if (i == 2) delta = third;
            else if (i == 3) delta = fourth;
            deltas.put(pid, (int) delta);
        }
        return deltas;
    }
}
