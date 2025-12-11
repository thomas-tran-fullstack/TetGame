package com.tetgame.modules.game.tienlen;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameEngine {

    private final com.tetgame.modules.room.service.RoomService roomService;

    // Start a Tiến Lên game in a room: deal and create GameState
    public GameState startTienLenGame(UUID roomId, List<UUID> players) {
        Deck deck = new Deck();
        deck.shuffle();
        int cardsPerPlayer = deck.getCards().size() / players.size();
        List<List<Card>> hands = deck.deal(players.size(), cardsPerPlayer);
        GameState state = new GameState(roomId, players);
        for (int i = 0; i < players.size(); i++) {
            state.setHand(players.get(i), hands.get(i));
        }
        return state;
    }

    public boolean validatePlay(GameState state, UUID playerId, List<Card> cards, Play currentTop) {
        Play play = Play.fromCards(cards);
        if (play.getType() == PlayType.INVALID) return false;
        // check player has cards
        List<Card> hand = state.getHand(playerId);
        if (hand == null || !hand.containsAll(cards)) return false;
        // if no current top, any valid play allowed
        if (currentTop == null) return true;
        return play.beats(currentTop);
    }

    /**
     * Xử lý đánh bài: validate + remove từ hand + set pile + next turn
     * Trả về true nếu thành công, false nếu validate failed
     */
    public boolean playMove(GameState state, UUID playerId, List<Card> cards) {
        Play play = Play.fromCards(cards);
        if (!validatePlay(state, playerId, cards, state.getCurrentPile())) {
            return false;
        }

        // Remove cards từ hand
        List<Card> hand = state.getHand(playerId);
        hand.removeAll(cards);

        // Update pile
        state.setCurrentPile(play);
        
        // Reset passed players (vì có lượt đánh mới)
        state.resetPassedThisTurn();
        
        // Log move
        state.logMove(playerId, cards);

        // Check if player won (0 cards left)
        if (hand.isEmpty()) {
            return true; // Game sẽ end, xử lý ở ngoài
        }

        // Next turn
        state.nextTurn();
        return true;
    }

    /**
     * Xử lý pass: mark người chơi đã pass
     * Nếu tất cả người khác pass → clear pile, reset turn
     */
    public boolean pass(GameState state, UUID playerId) {
        state.markPass(playerId);
        state.logMove(playerId, new ArrayList<>()); // log pass action
        
        // Nếu tất cả người khác pass
        if (state.areAllOthersPass(playerId)) {
            // Clear pile, reset passed set
            state.clearCurrentPile();
            state.resetPassedThisTurn();
            state.setCurrentTurnIndex(state.getTurnOrder().indexOf(playerId));
            // Current player (playerId) tiếp tục lượt
            return true;
        }

        // Next turn
        state.nextTurn();
        return false; // Game continues
    }

    /**
     * Kiểm tra kết thúc ván: ai hết bài trước
     * Trả về List<UUID> theo thứ tự xếp hạng (1st, 2nd, 3rd, 4th)
     * null nếu game chưa kết thúc
     */
    public List<UUID> checkGameEnd(GameState state) {
        List<UUID> rankings = new ArrayList<>();
        
        // Tìm những người có 0 cards
        for (UUID playerId : state.getTurnOrder()) {
            List<Card> hand = state.getHand(playerId);
            if (hand != null && hand.isEmpty()) {
                rankings.add(playerId);
            }
        }

        // Nếu có ít nhất 1 người thắng (hết bài) → game end
        // Rankings sẽ chứa những người hết bài theo thứ tự thắng
        if (rankings.size() > 0) {
            // Thêm những người còn lại vào rankings (theo số bài còn lại)
            var remaining = new ArrayList<>(state.getTurnOrder());
            remaining.removeAll(rankings);
            
            // Sort remaining by number of cards (ít cards = cao hơn)
            remaining.sort((a, b) -> Integer.compare(
                state.getHand(a).size(),
                state.getHand(b).size()
            ));
            rankings.addAll(remaining);
            
            return rankings;
        }
        return null; // Game continues
    }
}
