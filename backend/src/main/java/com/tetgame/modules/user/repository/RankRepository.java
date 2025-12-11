package com.tetgame.modules.user.repository;

import com.tetgame.modules.user.entity.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RankRepository extends JpaRepository<Rank, UUID> {
    Optional<Rank> findByUserId(UUID userId);
}
