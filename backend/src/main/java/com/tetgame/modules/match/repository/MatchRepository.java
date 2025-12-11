package com.tetgame.modules.match.repository;

import com.tetgame.modules.match.entity.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<MatchRecord, UUID> {
}
