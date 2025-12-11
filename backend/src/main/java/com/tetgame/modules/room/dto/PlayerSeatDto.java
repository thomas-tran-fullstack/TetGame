package com.tetgame.modules.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSeatDto {
    private Integer position;
    private UUID playerId;
    private String playerName;
    private String avatarUrl;
    private Boolean isReady;
}
