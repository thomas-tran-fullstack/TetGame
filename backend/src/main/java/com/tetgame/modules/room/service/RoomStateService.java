package com.tetgame.modules.room.service;

import com.tetgame.modules.room.dto.RoomResponse;
import com.tetgame.modules.game.tienlen.GameState;

import java.util.UUID;

public interface RoomStateService {
    Integer allocateSeat(UUID roomId, UUID playerId);
    void releaseSeat(UUID roomId, UUID playerId);
    boolean areAllPlayersReady(UUID roomId);
    RoomResponse startRoom(UUID roomId);
    GameState getGameState(UUID roomId);
}
