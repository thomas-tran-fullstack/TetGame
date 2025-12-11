package com.tetgame.modules.mission.service;

import com.tetgame.modules.mission.entity.UserTask;
import com.tetgame.modules.mission.entity.WheelSpin;

import java.util.List;
import java.util.UUID;

public interface MissionService {
    List<UserTask> getDailyTasks(UUID userId);
    UserTask claimTask(UUID userId, String taskKey) throws IllegalArgumentException;
    WheelSpin spinWheel(UUID userId);
}
