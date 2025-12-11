package com.tetgame.modules.room.service.impl;

import com.tetgame.modules.user.entity.User;
import com.tetgame.modules.user.repository.UserRepository;
import com.tetgame.modules.room.dto.CreateRoomRequest;
import com.tetgame.modules.room.dto.PlayerSeatDto;
import com.tetgame.modules.room.dto.RoomListItemDto;
import com.tetgame.modules.room.dto.RoomResponse;
import com.tetgame.modules.room.entity.GameType;
import com.tetgame.modules.room.entity.Room;
import com.tetgame.modules.room.entity.RoomSeat;
import com.tetgame.modules.room.entity.RoomStatus;
import com.tetgame.modules.room.exception.PlayerAlreadyInRoomException;
import com.tetgame.modules.room.exception.RoomFullException;
import com.tetgame.modules.room.exception.RoomNotFoundException;
import com.tetgame.modules.room.exception.RoomStateException;
import com.tetgame.modules.room.repository.RoomRepository;
import com.tetgame.modules.room.repository.RoomSeatRepository;
import com.tetgame.modules.room.service.RoomService;
import com.tetgame.websocket.RedisPublisher;
import com.tetgame.modules.room.service.RoomStateService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomServiceImpl implements RoomService {
    
    private final RoomRepository roomRepository;
    private final RoomSeatRepository roomSeatRepository;
    private final UserRepository userRepository;
    private final RedisPublisher redisPublisher;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RoomStateService roomStateService;
    
    @Override
    public RoomResponse createRoom(UUID hostId, CreateRoomRequest request) {
        // Validate inputs
        if (request.getName() == null || request.getName().length() < 3 || request.getName().length() > 100) {
            throw new IllegalArgumentException("Room name must be between 3 and 100 characters");
        }
        // Giới hạn số slot: từ 2 đến 4 (bao gồm người tạo phòng)
        if (request.getMaxPlayers() == null || request.getMaxPlayers() < 2 || request.getMaxPlayers() > 4) {
            throw new IllegalArgumentException("Max players must be between 2 and 4");
        }
        if (request.getGameType() == null) {
            throw new IllegalArgumentException("Game type is required");
        }
        
        User host = userRepository.findById(hostId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create room
        Room room = Room.builder()
            .name(request.getName())
            .gameType(request.getGameType())
            .hostId(hostId)
            .maxPlayers(request.getMaxPlayers())
            .betLevel(request.getBetLevel() == null ? com.tetgame.modules.room.entity.BetLevel.BAN1 : request.getBetLevel())
            .currentPlayers(1)
            .status(RoomStatus.WAITING)
            .build();
        
        room = roomRepository.save(room);
        
        // Create seats
        for (int i = 0; i < request.getMaxPlayers(); i++) {
            RoomSeat seat = RoomSeat.builder()
                .room(room)
                .position(i)
                .playerId(i == 0 ? hostId : null)
                .build();
            roomSeatRepository.save(seat);
        }
        
        RoomResponse resp = toRoomResponse(room, host.getUsername());
        try {
            String json = objectMapper.writeValueAsString(resp);
            String key = "room:" + room.getId();
            redisTemplate.opsForValue().set(key, json);
            redisTemplate.expire(key, Duration.ofHours(24));
            redisPublisher.publishRoomUpdate(room.getId().toString(), json);
            redisPublisher.publishRoomSeats(room.getId().toString(), json);
            redisPublisher.publishRoomPlayerList(room.getId().toString(), json);
        } catch (Exception ex) {
            // ignore cache error
        }
        return resp;
    }
    
    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomDetails(UUID roomId) {
        // Try cache first
        try {
            Object cached = redisTemplate.opsForValue().get("room:" + roomId);
            if (cached instanceof String) {
                return objectMapper.readValue((String) cached, RoomResponse.class);
            }
        } catch (Exception ex) {
            // ignore
        }

        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RoomNotFoundException("Room not found"));
        
        User host = userRepository.findById(room.getHostId())
            .orElseThrow(() -> new RuntimeException("Host not found"));
        
        RoomResponse resp = toRoomResponse(room, host.getUsername());
        try {
            String json = objectMapper.writeValueAsString(resp);
            String key = "room:" + room.getId();
            redisTemplate.opsForValue().set(key, json);
            redisTemplate.expire(key, Duration.ofHours(24));
        } catch (Exception ex) {
            // ignore
        }
        return resp;
    }
    
    @Override
    public RoomResponse joinRoom(UUID roomId, UUID playerId) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RoomNotFoundException("Room not found"));
        
        // Check if room is still waiting
        if (!room.getStatus().equals(RoomStatus.WAITING)) {
            throw new RoomStateException("Room is not accepting new players");
        }
        
        // Check if room is full
        if (room.getCurrentPlayers() >= room.getMaxPlayers()) {
            throw new RoomFullException("Room is full");
        }
        
        // Check if player already in room
        if (roomSeatRepository.playerInRoom(roomId, playerId)) {
            throw new PlayerAlreadyInRoomException("Player is already in this room");
        }
        
        User player = userRepository.findById(playerId)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        
        // Find available seat
        RoomSeat availableSeat = roomSeatRepository.findFirstAvailableSeat(roomId)
            .orElseThrow(() -> new RoomFullException("No available seats"));
        
        availableSeat.setPlayerId(playerId);
        roomSeatRepository.save(availableSeat);
        
        room.setCurrentPlayers(room.getCurrentPlayers() + 1);
        room = roomRepository.save(room);
        
        User host = userRepository.findById(room.getHostId())
            .orElseThrow(() -> new RuntimeException("Host not found"));
        RoomResponse resp = toRoomResponse(room, host.getUsername());
        try {
            String json = objectMapper.writeValueAsString(resp);
            String key = "room:" + room.getId();
            redisTemplate.opsForValue().set(key, json);
            redisTemplate.expire(key, Duration.ofHours(24));
            redisPublisher.publishRoomUpdate(room.getId().toString(), json);
            redisPublisher.publishRoomSeats(room.getId().toString(), json);
            redisPublisher.publishRoomPlayerList(room.getId().toString(), json);
        } catch (Exception ex) {
            // ignore
        }
        return resp;
    }
    
    @Override
    public RoomResponse leaveRoom(UUID roomId, UUID playerId) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RoomNotFoundException("Room not found"));
        
        RoomSeat seat = roomSeatRepository.findByRoomIdAndPlayerId(roomId, playerId)
            .orElseThrow(() -> new RoomStateException("Player is not in this room"));
        
        // If player is host, delete entire room
        if (room.getHostId().equals(playerId)) {
            roomRepository.delete(room);
            // notify deletion
            try {
                redisPublisher.publishRoomUpdate(room.getId().toString(), "{\"type\":\"room.deleted\",\"roomId\":\"" + room.getId() + "\"}");
            } catch (Exception ex) {}
            throw new RoomStateException("Room deleted because host left");
        }
        
        seat.setPlayerId(null);
        seat.setIsReady(false);
        roomSeatRepository.save(seat);
        
        room.setCurrentPlayers(room.getCurrentPlayers() - 1);
        room = roomRepository.save(room);
        
        User host = userRepository.findById(room.getHostId())
            .orElseThrow(() -> new RuntimeException("Host not found"));
        RoomResponse resp = toRoomResponse(room, host.getUsername());
        try {
            String json = objectMapper.writeValueAsString(resp);
            redisTemplate.opsForValue().set("room:" + room.getId(), json);
            redisPublisher.publishRoomUpdate(room.getId().toString(), json);
            redisPublisher.publishRoomSeats(room.getId().toString(), json);
            redisPublisher.publishRoomPlayerList(room.getId().toString(), json);
        } catch (Exception ex) {
            // ignore
        }
        return resp;
    }
    
    @Override
    public RoomResponse markReady(UUID roomId, UUID playerId, Boolean ready) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RoomNotFoundException("Room not found"));
        
        RoomSeat seat = roomSeatRepository.findByRoomIdAndPlayerId(roomId, playerId)
            .orElseThrow(() -> new RoomStateException("Player is not in this room"));
        
        seat.setIsReady(ready);
        roomSeatRepository.save(seat);
        
        User host = userRepository.findById(room.getHostId())
            .orElseThrow(() -> new RuntimeException("Host not found"));
        RoomResponse resp = toRoomResponse(room, host.getUsername());
        try {
            String json = objectMapper.writeValueAsString(resp);
            String key = "room:" + room.getId();
            redisTemplate.opsForValue().set(key, json);
            redisTemplate.expire(key, Duration.ofHours(24));
            redisPublisher.publishRoomUpdate(room.getId().toString(), json);
            redisPublisher.publishRoomSeats(room.getId().toString(), json);
        } catch (Exception ex) {
            // ignore
        }

        // If all ready, start the game
        try {
            if (roomStateService.areAllPlayersReady(roomId)) {
                roomStateService.startRoom(roomId);
            }
        } catch (Exception ex) {
            // ignore
        }

        return resp;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<RoomListItemDto> listRooms(GameType gameType, RoomStatus status, Pageable pageable) {
        Page<Room> rooms;
        
        if (gameType != null && status != null) {
            rooms = roomRepository.findByGameTypeAndStatus(gameType, status, pageable);
        } else if (gameType != null) {
            rooms = roomRepository.findByGameType(gameType, pageable);
        } else if (status != null) {
            rooms = roomRepository.findByStatus(status, pageable);
        } else {
            rooms = roomRepository.findAll(pageable);
        }
        
        return rooms.map(room -> {
            User host = userRepository.findById(room.getHostId()).orElse(null);
            return RoomListItemDto.builder()
                .id(room.getId())
                .name(room.getName())
                .gameType(room.getGameType())
                .hostName(host != null ? host.getUsername() : "Unknown")
                .currentPlayers(room.getCurrentPlayers())
                .maxPlayers(room.getMaxPlayers())
                .status(room.getStatus())
                .createdAt(room.getCreatedAt())
                .build();
        });
    }
    
    @Override
    public void deleteRoom(UUID roomId) {
        roomRepository.deleteById(roomId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer getPlayerCount(UUID roomId) {
        return roomSeatRepository.countOccupiedSeats(roomId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Boolean playerInRoom(UUID roomId, UUID playerId) {
        return roomSeatRepository.playerInRoom(roomId, playerId);
    }

    @Override
    public void addSpectator(UUID roomId, UUID spectatorId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException("Room not found"));
        var list = room.getSpectators();
        if (list == null) list = new java.util.ArrayList<>();
        if (!list.contains(spectatorId)) {
            list.add(spectatorId);
            room.setSpectators(list);
            roomRepository.save(room);
            try {
                redisPublisher.publishRoomPlayerList(room.getId().toString(), objectMapper.writeValueAsString(toRoomResponse(room, userRepository.findById(room.getHostId()).map(u->u.getUsername()).orElse(""))));
            } catch (Exception ex) {}
        }
    }

    @Override
    public void removeSpectator(UUID roomId, UUID spectatorId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException("Room not found"));
        var list = room.getSpectators();
        if (list != null && list.remove(spectatorId)) {
            room.setSpectators(list);
            roomRepository.save(room);
            try {
                redisPublisher.publishRoomPlayerList(room.getId().toString(), objectMapper.writeValueAsString(toRoomResponse(room, userRepository.findById(room.getHostId()).map(u->u.getUsername()).orElse(""))));
            } catch (Exception ex) {}
        }
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<java.util.UUID> listSpectators(UUID roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException("Room not found"));
        return room.getSpectators() == null ? java.util.Collections.emptyList() : room.getSpectators();
    }
    
    private RoomResponse toRoomResponse(Room room, String hostName) {
        var seats = roomSeatRepository.findByRoomIdOrderByPosition(room.getId())
            .stream()
            .map(seat -> {
                String playerName = null;
                String avatarUrl = null;
                if (seat.getPlayerId() != null) {
                    User player = userRepository.findById(seat.getPlayerId()).orElse(null);
                    if (player != null) {
                        playerName = player.getUsername();
                        avatarUrl = player.getAvatarUrl();
                    }
                }
                return PlayerSeatDto.builder()
                    .position(seat.getPosition())
                    .playerId(seat.getPlayerId())
                    .playerName(playerName)
                    .avatarUrl(avatarUrl)
                    .isReady(seat.getIsReady())
                    .build();
            })
            .collect(Collectors.toList());
        
        return RoomResponse.builder()
            .id(room.getId())
            .name(room.getName())
            .gameType(room.getGameType())
            .hostId(room.getHostId())
            .maxPlayers(room.getMaxPlayers())
            .currentPlayers(room.getCurrentPlayers())
            .status(room.getStatus())
            .seats(seats)
            .createdAt(room.getCreatedAt())
            .updatedAt(room.getUpdatedAt())
            .build();
    }
}
