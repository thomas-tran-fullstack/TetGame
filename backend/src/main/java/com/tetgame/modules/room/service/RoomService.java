package com.tetgame.modules.room.service;

import com.tetgame.modules.room.dto.CreateRoomRequest;
import com.tetgame.modules.room.dto.RoomListItemDto;
import com.tetgame.modules.room.dto.RoomResponse;
import com.tetgame.modules.room.entity.GameType;
import com.tetgame.modules.room.entity.Room;
import com.tetgame.modules.room.entity.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RoomService {
    RoomResponse createRoom(UUID hostId, CreateRoomRequest request);
    RoomResponse getRoomDetails(UUID roomId);
    RoomResponse joinRoom(UUID roomId, UUID playerId);
    RoomResponse leaveRoom(UUID roomId, UUID playerId);
    RoomResponse markReady(UUID roomId, UUID playerId, Boolean ready);
    void addSpectator(UUID roomId, UUID spectatorId);
    void removeSpectator(UUID roomId, UUID spectatorId);
    java.util.List<java.util.UUID> listSpectators(UUID roomId);
    Page<RoomListItemDto> listRooms(GameType gameType, RoomStatus status, Pageable pageable);
    void deleteRoom(UUID roomId);
    Integer getPlayerCount(UUID roomId);
    Boolean playerInRoom(UUID roomId, UUID playerId);
}
