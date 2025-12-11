package com.tetgame.modules.room.dto;

import com.tetgame.modules.room.entity.GameType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {
    private String name;
    private GameType gameType;
    private Integer maxPlayers;
    private com.tetgame.modules.room.entity.BetLevel betLevel;
}
