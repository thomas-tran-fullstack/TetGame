package com.tetgame.modules.mission.controller;

import com.tetgame.modules.mission.entity.UserTask;
import com.tetgame.modules.mission.entity.WheelSpin;
import com.tetgame.modules.mission.service.MissionService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/missions")
public class MissionController {
    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping("/daily")
    public List<UserTask> getDaily(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        return missionService.getDailyTasks(userId);
    }

    @PostMapping("/{taskKey}/claim")
    public UserTask claim(@PathVariable String taskKey, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        return missionService.claimTask(userId, taskKey);
    }

    @PostMapping("/spin")
    public WheelSpin spin(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        return missionService.spinWheel(userId);
    }
}
