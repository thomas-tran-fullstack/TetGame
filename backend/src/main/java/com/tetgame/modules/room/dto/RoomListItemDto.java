package com.tetgame.modules.room.dto;

import com.tetgame.modules.room.entity.GameType;
import com.tetgame.modules.room.entity.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomListItemDto {
    private UUID id;
    private String name;
    private GameType gameType;
    private String hostName;
    private Integer currentPlayers;
    private Integer maxPlayers;
    private RoomStatus status;
    private LocalDateTime createdAt;
}
