package com.tetgame.modules.mission.service.impl;

import com.tetgame.modules.mission.entity.TaskDefinition;
import com.tetgame.modules.mission.entity.UserTask;
import com.tetgame.modules.mission.entity.WheelSpin;
import com.tetgame.modules.mission.repository.TaskDefinitionRepository;
import com.tetgame.modules.mission.repository.UserTaskRepository;
import com.tetgame.modules.mission.repository.WheelSpinRepository;
import com.tetgame.modules.mission.service.MissionService;
import com.tetgame.modules.user.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class MissionServiceImpl implements MissionService {
    private final TaskDefinitionRepository taskDefinitionRepository;
    private final UserTaskRepository userTaskRepository;
    private final WheelSpinRepository wheelSpinRepository;
    private final WalletService walletService;

    public MissionServiceImpl(TaskDefinitionRepository taskDefinitionRepository,
                              UserTaskRepository userTaskRepository,
                              WheelSpinRepository wheelSpinRepository,
                              WalletService walletService) {
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.userTaskRepository = userTaskRepository;
        this.wheelSpinRepository = wheelSpinRepository;
        this.walletService = walletService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserTask> getDailyTasks(UUID userId) {
        // ensure tasks exist for user
        List<TaskDefinition> defs = taskDefinitionRepository.findAll();
        List<UserTask> userTasks = userTaskRepository.findByUserId(userId);

        Map<String, UserTask> map = new HashMap<>();
        for (UserTask ut : userTasks) map.put(ut.getTaskKey(), ut);

        List<UserTask> result = new ArrayList<>();
        for (TaskDefinition def : defs) {
            if (map.containsKey(def.getTaskKey())) result.add(map.get(def.getTaskKey()));
            else {
                UserTask ut = UserTask.builder()
                        .userId(userId)
                        .taskKey(def.getTaskKey())
                        .completed(false)
                        .progress(0)
                        .createdAt(OffsetDateTime.now())
                        .build();
                userTaskRepository.save(ut);
                result.add(ut);
            }
        }

        return result;
    }

    @Override
    @Transactional
    public UserTask claimTask(UUID userId, String taskKey) throws IllegalArgumentException {
        Optional<TaskDefinition> defOpt = taskDefinitionRepository.findByTaskKey(taskKey);
        if (defOpt.isEmpty()) throw new IllegalArgumentException("Task not found");
        TaskDefinition def = defOpt.get();
        Optional<UserTask> utOpt = userTaskRepository.findByUserIdAndTaskKey(userId, taskKey);
        if (utOpt.isEmpty()) throw new IllegalArgumentException("User task not found");
        UserTask ut = utOpt.get();
        if (ut.isCompleted()) throw new IllegalArgumentException("Already claimed");

        // mark complete and credit wallet
        ut.setCompleted(true);
        ut.setCompletedAt(OffsetDateTime.now());
        userTaskRepository.save(ut);

        if (def.getReward() > 0) {
            walletService.add(userId, def.getReward());
        }

        return ut;
    }

    @Override
    @Transactional
    public WheelSpin spinWheel(UUID userId) {
        // simple wheel with fixed segments
        List<WheelSegment> segments = List.of(
                new WheelSegment("Small", 100, 40),
                new WheelSegment("Medium", 500, 30),
                new WheelSegment("Large", 2000, 20),
                new WheelSegment("Jackpot", 10000, 10)
        );

        int totalWeight = segments.stream().mapToInt(s -> s.weight).sum();
        int r = new Random().nextInt(totalWeight);
        int acc = 0;
        WheelSegment chosen = segments.get(0);
        for (WheelSegment s : segments) {
            acc += s.weight;
            if (r < acc) { chosen = s; break; }
        }

        WheelSpin spin = WheelSpin.builder()
                .userId(userId)
                .prize(chosen.name)
                .amount(chosen.amount)
                .createdAt(OffsetDateTime.now())
                .build();
        wheelSpinRepository.save(spin);

        if (chosen.amount > 0) walletService.add(userId, chosen.amount);

        return spin;
    }

    static class WheelSegment {
        String name; long amount; int weight;
        WheelSegment(String n, long a, int w){ name=n; amount=a; weight=w; }
    }
}
