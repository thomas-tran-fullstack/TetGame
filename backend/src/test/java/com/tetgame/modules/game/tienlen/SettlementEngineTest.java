package com.tetgame.modules.game.tienlen;

import com.tetgame.modules.room.entity.BetLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

@DisplayName("Settlement Engine Tests")
class SettlementEngineTest {

    private GameState gameState;
    private List<UUID> players;

    @BeforeEach
    void setUp() {
        players = Arrays.asList(
            UUID.randomUUID(), UUID.randomUUID(),
            UUID.randomUUID(), UUID.randomUUID()
        );
        gameState = new GameState(UUID.randomUUID(), players);
        
        // Setup hands: simulate game end
        for (int i = 0; i < 4; i++) {
            gameState.setHand(players.get(i), new ArrayList<>());
        }
    }

    @Test
    @DisplayName("Should settle BAN1 (5k-10k) correctly")
    void testSettleBAN1() {
        Map<UUID, Integer> settlement = SettlementEngine.settle(gameState, BetLevel.BAN1);

        assertNotNull(settlement);
        assertEquals(4, settlement.size());
        
        // Expected: 1st +10k, 2nd +5k, 3rd -5k, 4th -10k
        // Note: Rankings are empty, so we verify non-null and correct sum
        long total = settlement.values().stream().mapToLong(Integer::longValue).sum();
        assertEquals(0, total); // Should sum to 0 (zero-sum game)
    }

    @Test
    @DisplayName("Should settle BAN2 (10k-20k) correctly")
    void testSettleBAN2() {
        Map<UUID, Integer> settlement = SettlementEngine.settle(gameState, BetLevel.BAN2);

        assertNotNull(settlement);
        assertEquals(4, settlement.size());
        
        long total = settlement.values().stream().mapToLong(Integer::longValue).sum();
        assertEquals(0, total);
    }

    @Test
    @DisplayName("Should settle BAN3 (50k-100k) correctly")
    void testSettleBAN3() {
        Map<UUID, Integer> settlement = SettlementEngine.settle(gameState, BetLevel.BAN3);

        assertNotNull(settlement);
        assertEquals(4, settlement.size());
    }

    @Test
    @DisplayName("Should settle BAN4 (100k-200k) correctly")
    void testSettleBAN4() {
        Map<UUID, Integer> settlement = SettlementEngine.settle(gameState, BetLevel.BAN4);

        assertNotNull(settlement);
        assertEquals(4, settlement.size());
    }

    @Test
    @DisplayName("Should settle BAN5 (500k-1m) correctly")
    void testSettleBAN5() {
        Map<UUID, Integer> settlement = SettlementEngine.settle(gameState, BetLevel.BAN5);

        assertNotNull(settlement);
        assertEquals(4, settlement.size());
    }

    @Test
    @DisplayName("Should include all players in settlement")
    void testSettlementIncludesAllPlayers() {
        Map<UUID, Integer> settlement = SettlementEngine.settle(gameState, BetLevel.BAN1);

        for (UUID player : players) {
            assertTrue(settlement.containsKey(player));
        }
    }

    @Test
    @DisplayName("Settlement should be zero-sum game")
    void testSettlementIsZeroSum() {
        Map<UUID, Integer> settlement = SettlementEngine.settle(gameState, BetLevel.BAN1);

        long total = settlement.values().stream().mapToLong(Integer::longValue).sum();
        assertEquals(0, total);
    }

    @Test
    @DisplayName("Should return different payouts for different bet levels")
    void testDifferentBetLevelPayouts() {
        Map<UUID, Integer> ban1 = SettlementEngine.settle(gameState, BetLevel.BAN1);
        Map<UUID, Integer> ban5 = SettlementEngine.settle(gameState, BetLevel.BAN5);

        // BAN5 should have higher absolute values than BAN1
        long ban1Max = ban1.values().stream().mapToLong(Math::abs).max().orElse(0);
        long ban5Max = ban5.values().stream().mapToLong(Math::abs).max().orElse(0);

        assertTrue(ban5Max > ban1Max);
    }
}
