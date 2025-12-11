package com.tetgame.modules.room.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomPlayerJoinedEvent {
    private UUID roomId;
    private UUID playerId;
    private Integer position;
    private String playerName;
}
