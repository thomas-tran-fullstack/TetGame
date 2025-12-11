package com.tetgame.modules.mission.repository;

import com.tetgame.modules.mission.entity.WheelSpin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface WheelSpinRepository extends JpaRepository<WheelSpin, UUID> {
    List<WheelSpin> findByUserId(UUID userId);
}
