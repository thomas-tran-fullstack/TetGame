package com.tetgame.modules.mission.repository;

import com.tetgame.modules.mission.entity.TaskDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TaskDefinitionRepository extends JpaRepository<TaskDefinition, UUID> {
    Optional<TaskDefinition> findByTaskKey(String taskKey);
}
