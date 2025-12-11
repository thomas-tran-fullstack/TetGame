package com.tetgame.modules.match.repository;

import com.tetgame.modules.match.entity.MatchPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MatchPlayerRepository extends JpaRepository<MatchPlayer, UUID> {
    List<MatchPlayer> findByUserId(UUID userId);
    List<MatchPlayer> findByMatchId(UUID matchId);
}
