package com.tetgame.modules.room.controller;

import com.tetgame.modules.room.dto.CreateRoomRequest;
import com.tetgame.modules.room.dto.PageResponse;
import com.tetgame.modules.room.dto.RoomListItemDto;
import com.tetgame.modules.room.dto.RoomResponse;
import com.tetgame.modules.room.entity.GameType;
import com.tetgame.modules.room.entity.RoomStatus;
import com.tetgame.modules.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    
    private final RoomService roomService;
    
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
        @RequestBody CreateRoomRequest request,
        Authentication authentication) {
        
        UUID userId = UUID.fromString(authentication.getName());
        RoomResponse response = roomService.createRoom(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoomDetails(@PathVariable UUID roomId) {
        RoomResponse response = roomService.getRoomDetails(roomId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{roomId}/join")
    public ResponseEntity<RoomResponse> joinRoom(
        @PathVariable UUID roomId,
        Authentication authentication) {
        
        UUID userId = UUID.fromString(authentication.getName());
        RoomResponse response = roomService.joinRoom(roomId, userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<RoomResponse> leaveRoom(
        @PathVariable UUID roomId,
        Authentication authentication) {
        
        UUID userId = UUID.fromString(authentication.getName());
        RoomResponse response = roomService.leaveRoom(roomId, userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{roomId}/ready")
    public ResponseEntity<RoomResponse> markReady(
        @PathVariable UUID roomId,
        @RequestParam Boolean ready,
        Authentication authentication) {
        
        UUID userId = UUID.fromString(authentication.getName());
        RoomResponse response = roomService.markReady(roomId, userId, ready);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{roomId}/spectators")
    public ResponseEntity<Void> addSpectator(@PathVariable UUID roomId, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        roomService.addSpectator(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roomId}/spectators")
    public ResponseEntity<Void> removeSpectator(@PathVariable UUID roomId, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        roomService.removeSpectator(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roomId}/spectators")
    public ResponseEntity<java.util.List<java.util.UUID>> listSpectators(@PathVariable UUID roomId) {
        return ResponseEntity.ok(roomService.listSpectators(roomId));
    }
    
    @GetMapping
    public ResponseEntity<PageResponse<RoomListItemDto>> listRooms(
        @RequestParam(required = false) GameType gameType,
        @RequestParam(required = false) RoomStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<RoomListItemDto> roomPage = roomService.listRooms(gameType, status, pageable);
        
        PageResponse<RoomListItemDto> response = PageResponse.<RoomListItemDto>builder()
            .content(roomPage.getContent())
            .pageNumber(roomPage.getNumber())
            .pageSize(roomPage.getSize())
            .totalElements(roomPage.getTotalElements())
            .totalPages(roomPage.getTotalPages())
            .isFirst(roomPage.isFirst())
            .isLast(roomPage.isLast())
            .build();
        
        return ResponseEntity.ok(response);
    }
}
