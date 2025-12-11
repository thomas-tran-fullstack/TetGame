package com.tetgame.modules.room.dto;

import com.tetgame.modules.room.entity.GameType;
import com.tetgame.modules.room.entity.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    private UUID id;
    private String name;
    private GameType gameType;
    private UUID hostId;
    private Integer maxPlayers;
    private com.tetgame.modules.room.entity.BetLevel betLevel;
    private Integer currentPlayers;
    private RoomStatus status;
    private List<PlayerSeatDto> seats;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
