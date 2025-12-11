package com.tetgame.modules.room.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tetgame.modules.room.dto.RoomResponse;
import com.tetgame.modules.room.entity.Room;
import com.tetgame.modules.room.entity.RoomSeat;
import com.tetgame.modules.room.entity.RoomStatus;
import com.tetgame.modules.game.tienlen.Card;
import com.tetgame.modules.game.tienlen.HandValidator;
import com.tetgame.modules.game.tienlen.SettlementEngine;
import com.tetgame.modules.game.tienlen.GameState;
import com.tetgame.modules.room.repository.RoomRepository;
import com.tetgame.modules.room.exception.RoomStateException;
import com.tetgame.modules.room.repository.RoomSeatRepository;
import com.tetgame.modules.room.service.RoomStateService;
import com.tetgame.websocket.RedisPublisher;
import com.tetgame.modules.game.tienlen.GameEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomStateServiceImpl implements RoomStateService {

    private final RoomRepository roomRepository;
    private final RoomSeatRepository roomSeatRepository;
    private final RedisPublisher redisPublisher;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final GameEngine gameEngine;
    private final com.tetgame.modules.user.service.WalletService walletService;

    @Override
    public Integer allocateSeat(UUID roomId, UUID playerId) {
        Optional<RoomSeat> seatOpt = roomSeatRepository.findFirstAvailableSeat(roomId);
        if (seatOpt.isEmpty()) return -1;
        RoomSeat seat = seatOpt.get();
        seat.setPlayerId(playerId);
        roomSeatRepository.save(seat);

        Room room = roomRepository.findById(roomId).orElseThrow();
        room.setCurrentPlayers(room.getCurrentPlayers() + 1);
        roomRepository.save(room);

        // publish seat allocation event and update a small cache entry
        try {
            var event = java.util.Map.of(
                "type", "room.seat-allocated",
                "roomId", room.getId().toString(),
                "position", seat.getPosition(),
                "playerId", playerId.toString()
            );
            String json = objectMapper.writeValueAsString(event);
            String key = "room:" + room.getId();
            redisTemplate.opsForValue().set(key, json);
            redisTemplate.expire(key, Duration.ofHours(24));
            redisPublisher.publishRoomSeats(room.getId().toString(), json);
            redisPublisher.publishRoomPlayerList(room.getId().toString(), json);
            redisPublisher.publishRoomUpdate(room.getId().toString(), json);
        } catch (Exception ex) {
            // ignore
        }

        return seat.getPosition();
    }

    @Override
    public void releaseSeat(UUID roomId, UUID playerId) {
        Optional<RoomSeat> seatOpt = roomSeatRepository.findByRoomIdAndPlayerId(roomId, playerId);
        if (seatOpt.isPresent()) {
            RoomSeat seat = seatOpt.get();
            seat.setPlayerId(null);
            seat.setIsReady(false);
            roomSeatRepository.save(seat);

            Room room = roomRepository.findById(roomId).orElseThrow();
            room.setCurrentPlayers(Math.max(0, room.getCurrentPlayers() - 1));
            roomRepository.save(room);

            try {
                var event = java.util.Map.of(
                    "type", "room.seat-released",
                    "roomId", room.getId().toString(),
                    "position", seat.getPosition(),
                    "playerId", playerId.toString()
                );
                String json = objectMapper.writeValueAsString(event);
                String key = "room:" + room.getId();
                redisTemplate.opsForValue().set(key, json);
                redisTemplate.expire(key, Duration.ofHours(24));
                redisPublisher.publishRoomSeats(room.getId().toString(), json);
                redisPublisher.publishRoomPlayerList(room.getId().toString(), json);
                redisPublisher.publishRoomUpdate(room.getId().toString(), json);
            } catch (Exception ex) {}
        }
    }

    @Override
    public boolean areAllPlayersReady(UUID roomId) {
        List<RoomSeat> seats = roomSeatRepository.findByRoomIdOrderByPosition(roomId);
        long occupied = seats.stream().filter(s -> s.getPlayerId() != null).count();
        if (occupied < 2) return false;
        return seats.stream().filter(s -> s.getPlayerId() != null).allMatch(s -> Boolean.TRUE.equals(s.getIsReady()));
    }

    @Override
    public RoomResponse startRoom(UUID roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        // Trước khi bắt đầu ván đấu, đảm bảo số người hợp lệ
        int occupied = roomSeatRepository.countOccupiedSeats(roomId);
        if (occupied < 2) {
            throw new RoomStateException("Không đủ người để bắt đầu ván (cần ít nhất 2 người)");
        }
        if (occupied > 4) {
            throw new RoomStateException("Phòng chỉ cho phép tối đa 4 người chơi để bắt đầu ván");
        }

        room.setStatus(RoomStatus.PLAYING);
        roomRepository.save(room);

        try {
            // Lấy danh sách người chơi theo vị trí ghế
            var seats = roomSeatRepository.findByRoomIdOrderByPosition(roomId);
            var players = seats.stream()
                .filter(s -> s.getPlayerId() != null)
                .map(RoomSeat::getPlayerId)
                .toList();

            // Start game bằng GameEngine và lấy GameState
            var gameState = gameEngine.startTienLenGame(room.getId(), players);
            
            // Save GameState to Redis for retrieval during game
            String gameStateKey = "game:" + room.getId() + ":state";
            redisTemplate.opsForValue().set(gameStateKey, gameState);
            redisTemplate.expire(gameStateKey, Duration.ofHours(2));

            // Chuẩn bị payload: ánh xạ playerId -> danh sách lá (string)
            var handsMap = new HashMap<String, List<String>>();
            gameState.getHands().forEach((pid, hand) -> {
                var list = hand.stream().map(Card::toString).toList();
                handsMap.put(pid.toString(), list);
            });

            var event = Map.of(
                "type", "game.started",
                "roomId", room.getId().toString(),
                "hands", handsMap
            );
            String json = objectMapper.writeValueAsString(event);
            String key = "room:" + room.getId();
            redisTemplate.opsForValue().set(key, json);
            redisTemplate.expire(key, Duration.ofHours(24));

            // Nếu có người tới trắng (instant win), publish game.ended thay vì normal started
            var winners = gameState.getHands().entrySet().stream()
                .filter(e -> HandValidator.laToiTrang(e.getValue()))
                .map(e -> e.getKey())
                .toList();

            if (!winners.isEmpty()) {
                // Settlement
                    var settleMap = SettlementEngine.settle(gameState, room.getBetLevel());
                // Áp settlement vào ví người chơi
                try {
                    settleMap.forEach((uid, delta) -> {
                        if (delta > 0) walletService.add(uid, delta);
                        else if (delta < 0) walletService.subtract(uid, -delta);
                    });
                } catch (Exception ex) {
                    // ignore wallet errors for now
                }
                var endEvent = Map.of(
                    "type", "game.ended",
                    "roomId", room.getId().toString(),
                    "winners", winners.stream().map(UUID::toString).toList(),
                    "settlement", settleMap
                );
                String endJson = objectMapper.writeValueAsString(endEvent);
                redisTemplate.opsForValue().set(key, endJson);
                redisTemplate.expire(key, Duration.ofHours(24));
                redisPublisher.publishGameEnded(room.getId().toString(), endJson);
                redisPublisher.publishRoomUpdate(room.getId().toString(), endJson);
                return objectMapper.readValue(endJson, RoomResponse.class);
            }

            // Normal start
            redisPublisher.publishGameStarted(room.getId().toString(), json);
            redisPublisher.publishRoomUpdate(room.getId().toString(), json);
            return objectMapper.readValue(json, RoomResponse.class);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public GameState getGameState(UUID roomId) {
        String gameStateKey = "game:" + roomId + ":state";
        Object obj = redisTemplate.opsForValue().get(gameStateKey);
        if (obj instanceof GameState) {
            return (GameState) obj;
        }
        return null;
    }
}
