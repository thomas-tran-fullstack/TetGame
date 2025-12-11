package com.tetgame.modules.mission.repository;

import com.tetgame.modules.mission.entity.UserTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTaskRepository extends JpaRepository<UserTask, UUID> {
    List<UserTask> findByUserId(UUID userId);
    Optional<UserTask> findByUserIdAndTaskKey(UUID userId, String taskKey);
}
