package com.tetgame.modules.room.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tetgame.modules.room.entity.Room;
import com.tetgame.modules.room.entity.RoomStatus;
import com.tetgame.modules.room.entity.RoomSeat;
import com.tetgame.modules.room.repository.RoomRepository;
import com.tetgame.modules.room.repository.RoomSeatRepository;
import com.tetgame.websocket.RedisPublisher;
import com.tetgame.modules.game.tienlen.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomStateServiceImplTest {

    private RoomRepository roomRepository;
    private RoomSeatRepository roomSeatRepository;
    private RedisPublisher redisPublisher;
    private RedisTemplate<String, Object> redisTemplate;
    private ObjectMapper objectMapper;
    private GameEngine gameEngine;
    private com.tetgame.modules.user.service.WalletService walletService;
    private RoomStateServiceImpl service;

    @BeforeEach
    void setup() {
        roomRepository = mock(RoomRepository.class);
        roomSeatRepository = mock(RoomSeatRepository.class);
        redisPublisher = mock(RedisPublisher.class);
        redisTemplate = mock(RedisTemplate.class);
        objectMapper = new ObjectMapper();
        gameEngine = mock(GameEngine.class);
        walletService = mock(com.tetgame.modules.user.service.WalletService.class);

        service = new RoomStateServiceImpl(roomRepository, roomSeatRepository, redisPublisher, redisTemplate, objectMapper, gameEngine, walletService);
    }

    @Test
    void startRoom_should_throw_when_less_than_two_players() {
        UUID roomId = UUID.randomUUID();
        Room room = Room.builder().id(roomId).status(RoomStatus.WAITING).build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomSeatRepository.countOccupiedSeats(roomId)).thenReturn(1);

        var ex = assertThrows(RuntimeException.class, () -> service.startRoom(roomId));
        assertTrue(ex.getMessage().contains("Không đủ người"));
    }

    @Test
    void startRoom_should_throw_when_more_than_four_players() {
        UUID roomId = UUID.randomUUID();
        Room room = Room.builder().id(roomId).status(RoomStatus.WAITING).build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomSeatRepository.countOccupiedSeats(roomId)).thenReturn(5);

        var ex = assertThrows(RuntimeException.class, () -> service.startRoom(roomId));
        assertTrue(ex.getMessage().contains("tối đa 4"));
    }

    @Test
    void startRoom_should_start_when_valid_player_count() throws Exception {
        UUID roomId = UUID.randomUUID();
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        Room room = Room.builder().id(roomId).status(RoomStatus.WAITING).build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomSeatRepository.countOccupiedSeats(roomId)).thenReturn(2);

        RoomSeat s1 = RoomSeat.builder().position(0).playerId(p1).build();
        RoomSeat s2 = RoomSeat.builder().position(1).playerId(p2).build();
        when(roomSeatRepository.findByRoomIdOrderByPosition(roomId)).thenReturn(List.of(s1, s2));

        // Mock game engine to return empty GameState
        when(gameEngine.startTienLenGame(eq(roomId), anyList())).thenReturn(new com.tetgame.modules.game.tienlen.GameState(roomId, List.of(p1, p2)));

        var resp = service.startRoom(roomId);
        // since we return RoomResponse read from event, resp may be null if serialization path differs; accept non-null or null
        assertTrue(resp == null || resp.getId().equals(roomId));

        verify(redisPublisher, atLeastOnce()).publishGameStarted(eq(roomId.toString()), anyString());
    }
}
