package com.tetgame.modules.game.tienlen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

@DisplayName("GameEngine Tests")
class GameEngineTest {

    private GameEngine gameEngine;
    
    @Mock
    private com.tetgame.modules.room.service.RoomService roomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gameEngine = new GameEngine(roomService);
    }

    // ========== START GAME TESTS ==========

    @Test
    @DisplayName("Should start game with 4 players and deal cards")
    void testStartGameWith4Players() {
        UUID room = UUID.randomUUID();
        List<UUID> players = Arrays.asList(
            UUID.randomUUID(), UUID.randomUUID(), 
            UUID.randomUUID(), UUID.randomUUID()
        );

        GameState state = gameEngine.startTienLenGame(room, players);

        assertNotNull(state);
        assertEquals(room, state.getRoomId());
        assertEquals(4, state.getTurnOrder().size());
        assertEquals(0, state.getCurrentTurnIndex());
        
        // Each player should have 13 cards (52 / 4)
        for (UUID player : players) {
            List<Card> hand = state.getHand(player);
            assertNotNull(hand);
            assertEquals(13, hand.size());
        }
    }

    @Test
    @DisplayName("Should start game with 2 players")
    void testStartGameWith2Players() {
        UUID room = UUID.randomUUID();
        List<UUID> players = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());

        GameState state = gameEngine.startTienLenGame(room, players);

        assertNotNull(state);
        assertEquals(2, state.getTurnOrder().size());
        
        // Each player should have 26 cards (52 / 2)
        for (UUID player : players) {
            assertEquals(26, state.getHand(player).size());
        }
    }

    @Test
    @DisplayName("Should start game with 3 players")
    void testStartGameWith3Players() {
        UUID room = UUID.randomUUID();
        List<UUID> players = Arrays.asList(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );

        GameState state = gameEngine.startTienLenGame(room, players);

        assertEquals(3, state.getTurnOrder().size());
        assertEquals(17, state.getHand(players.get(0)).size());
        assertEquals(17, state.getHand(players.get(1)).size());
        assertEquals(17, state.getHand(players.get(2)).size());
    }

    // ========== VALIDATE PLAY TESTS ==========

    @Test
    @DisplayName("Should validate valid single card play when no current pile")
    void testValidateSingleCardNoCurrentPile() {
        UUID room = UUID.randomUUID();
        List<UUID> players = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        GameState state = gameEngine.startTienLenGame(room, players);

        UUID player = players.get(0);
        List<Card> hand = state.getHand(player);
        Card cardToPlay = hand.get(0);

        boolean valid = gameEngine.validatePlay(state, player, Arrays.asList(cardToPlay), null);

        assertTrue(valid);
    }

    @Test
    @DisplayName("Should reject invalid play when cards not in hand")
    void testValidatePlayCardsNotInHand() {
        UUID room = UUID.randomUUID();
        List<UUID> players = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        GameState state = gameEngine.startTienLenGame(room, players);

        UUID player = players.get(0);
        Card fakeCard = new Card(CardSuit.SPADES, CardRank.ACE);

        boolean valid = gameEngine.validatePlay(state, player, Arrays.asList(fakeCard), null);

        assertFalse(valid);
    }

    @Test
    @DisplayName("Should reject play that doesn't beat current pile")
    void testValidatePlayDoesntBeatPile() {
        UUID room = UUID.randomUUID();
        List<UUID> players = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        GameState state = gameEngine.startTienLenGame(room, players);

        UUID player = players.get(0);
        List<Card> hand = state.getHand(player);

        // Set a high current pile
        Play highPile = new Play(PlayType.SINGLE, 
            Arrays.asList(new Card(CardSuit.SPADES, CardRank.TWO)), 15);
        state.setCurrentPile(highPile);

        // Try to play a low card
        Card lowCard = new Card(CardSuit.HEARTS, CardRank.THREE);
        boolean valid = gameEngine.validatePlay(state, player, 
            Arrays.asList(lowCard), highPile);

        assertFalse(valid);
    }

    // ========== PLAY MOVE TESTS ==========

    @Test
    @DisplayName("Should execute valid play move and advance turn")
    void testPlayMoveValid() {
        UUID room = UUID.randomUUID();
        List<UUID> players = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        GameState state = gameEngine.startTienLenGame(room, players);

        UUID player = players.get(0);
        List<Card> hand = state.getHand(player);
        int initialHandSize = hand.size();
        Card cardToPlay = hand.get(0);

        boolean success = gameEngine.playMove(state, player, Arrays.asList(cardToPlay));

        assertTrue(success);
        assertEquals(initialHandSize - 1, hand.size());
        assertFalse(hand.contains(cardToPlay));
        assertEquals(players.get(1), state.getCurrentPlayer());
    }

    @Test
    @DisplayName("Should not execute invalid play move")
    void testPlayMoveInvalid() {
        UUID room = UUID.randomUUID();
        List<UUID> players = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        GameState state = gameEngine.startTienLenGame(room, players);

        UUID player = players.get(0);
        Card fakeCard = new Card(CardSuit.SPADES, CardRank.ACE);
        int initialHandSize = state.getHand(player).size();

        boolean success = gameEngine.playMove(state, player, Arrays.asList(fakeCard));

        assertFalse(success);
        assertEquals(initialHandSize, state.getHand(player).size());
    }

    @Test
    @DisplayName("Should log play move")
    void testPlayMoveLogsAction() {
        UUID room = UUID.randomUUID();
        List<UUID> players = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        GameState state = gameEngine.startTienLenGame(room, players);

        UUID player = players.get(0);
        List<Card> hand = state.getHand(player);
        Card cardToPlay = hand.get(0);

        gameEngine.playMove(state, player, Arrays.asList(cardToPlay));

        assertFalse(state.getGameLog().isEmpty());
        Map<String, Object> lastLog = state.getGameLog().get(0);
        assertEquals(player.toString(), lastLog.get("playerId"));
    }

    @Test
    @DisplayName("Should win when player plays last card")
    void testPlayMoveWinCondition() {
        UUID room = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        List<UUID> players = Arrays.asList(player1, player2);
        GameState state = gameEngine.startTienLenGame(room, players);

        // Remove all but one card from player1
        List<Card> hand = state.getHand(player1);
        Card lastCard = hand.get(0);
        hand.clear();
        hand.add(lastCard);

        boolean success = gameEngine.playMove(state, player1, Arrays.asList(lastCard));

        assertTrue(success);
        assertTrue(hand.isEmpty());
    }

    // ========== PASS TESTS ==========

    @Test
    @DisplayName("Should mark player as passed")
    void testPassMarkPlayer() {
        UUID room = UUID.randomUUID();
        List<UUID> players = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        GameState state = gameEngine.startTienLenGame(room, players);

        UUID player = players.get(0);
        Set<UUID> passedBefore = new HashSet<>(state.getPassedThisTurn());

        gameEngine.pass(state, player);

        Set<UUID> passedAfter = state.getPassedThisTurn();
        assertTrue(passedAfter.contains(player));
    }

    @Test
    @DisplayName("Should advance turn after pass if not all passed")
    void testPassAdvanceTurn() {
        UUID room = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        List<UUID> players = Arrays.asList(player1, player2);
        GameState state = gameEngine.startTienLenGame(room, players);

        UUID currentBeforePass = state.getCurrentPlayer();
        gameEngine.pass(state, player1);
        UUID currentAfterPass = state.getCurrentPlayer();

        assertNotEquals(currentBeforePass, currentAfterPass);
    }

    @Test
    @DisplayName("Should clear pile when all other players pass")
    void testPassAllOtherPassClearsPile() {
        UUID room = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        List<UUID> players = Arrays.asList(player1, player2);
        GameState state = gameEngine.startTienLenGame(room, players);

        // Set current pile
        Play pile = new Play(PlayType.SINGLE, 
            Arrays.asList(new Card(CardSuit.SPADES, CardRank.THREE)), 3);
        state.setCurrentPile(pile);

        // Player2 passes (only player left is player1)
        gameEngine.pass(state, player2);

        assertNull(state.getCurrentPile());
    }

    // ========== CHECK GAME END TESTS ==========

    @Test
    @DisplayName("Should detect game end when player has 0 cards")
    void testCheckGameEndDetectsWinner() {
        UUID room = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        List<UUID> players = Arrays.asList(player1, player2);
        GameState state = gameEngine.startTienLenGame(room, players);

        // Make player1 win
        state.getHand(player1).clear();

        List<UUID> rankings = gameEngine.checkGameEnd(state);

        assertNotNull(rankings);
        assertEquals(2, rankings.size());
        assertEquals(player1, rankings.get(0));
        assertEquals(player2, rankings.get(1));
    }

    @Test
    @DisplayName("Should return null when game not ended")
    void testCheckGameEndGameNotEnded() {
        UUID room = UUID.randomUUID();
        List<UUID> players = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        GameState state = gameEngine.startTienLenGame(room, players);

        List<UUID> rankings = gameEngine.checkGameEnd(state);

        assertNull(rankings);
    }

    @Test
    @DisplayName("Should rank players by remaining cards")
    void testCheckGameEndRankingByCards() {
        UUID room = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        UUID player3 = UUID.randomUUID();
        UUID player4 = UUID.randomUUID();
        List<UUID> players = Arrays.asList(player1, player2, player3, player4);
        GameState state = gameEngine.startTienLenGame(room, players);

        // Player1: 0 cards (winner)
        // Player2: 2 cards
        // Player3: 5 cards
        // Player4: 6 cards
        state.getHand(player1).clear();
        state.getHand(player2).retainAll(state.getHand(player2).subList(0, 2));
        state.getHand(player3).retainAll(state.getHand(player3).subList(0, 5));
        state.getHand(player4).retainAll(state.getHand(player4).subList(0, 6));

        List<UUID> rankings = gameEngine.checkGameEnd(state);

        assertNotNull(rankings);
        assertEquals(4, rankings.size());
        assertEquals(player1, rankings.get(0)); // 1st: 0 cards
        assertEquals(player2, rankings.get(1)); // 2nd: 2 cards
        assertEquals(player3, rankings.get(2)); // 3rd: 5 cards
        assertEquals(player4, rankings.get(3)); // 4th: 6 cards
    }

    // ========== GAME FLOW TESTS ==========

    @Test
    @DisplayName("Should complete full game flow: play -> pass -> end")
    void testFullGameFlow() {
        UUID room = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        List<UUID> players = Arrays.asList(player1, player2);
        GameState state = gameEngine.startTienLenGame(room, players);

        // Player1 plays
        List<Card> hand1 = state.getHand(player1);
        Card card = hand1.get(0);
        gameEngine.playMove(state, player1, Arrays.asList(card));

        assertEquals(player2, state.getCurrentPlayer());

        // Player2 passes
        gameEngine.pass(state, player2);

        // Pile should be cleared
        assertNull(state.getCurrentPile());
        // Turn back to player1
        assertEquals(player1, state.getCurrentPlayer());
    }

    @Test
    @DisplayName("Should handle multiple rounds")
    void testMultipleRounds() {
        UUID room = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        List<UUID> players = Arrays.asList(player1, player2);
        GameState state = gameEngine.startTienLenGame(room, players);

        // Round 1: Player1 plays, Player2 plays higher
        List<Card> hand1 = state.getHand(player1);
        Card low = hand1.get(0);
        gameEngine.playMove(state, player1, Arrays.asList(low));

        assertEquals(player2, state.getCurrentPlayer());

        // Player2 plays higher card
        List<Card> hand2 = state.getHand(player2);
        Card high = hand2.get(hand2.size() - 1); // Try last card
        gameEngine.playMove(state, player2, Arrays.asList(high));

        assertEquals(player1, state.getCurrentPlayer());
    }
}
