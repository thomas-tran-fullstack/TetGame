package com.tetgame.modules.room.repository;

import com.tetgame.modules.room.entity.Room;
import com.tetgame.modules.room.entity.GameType;
import com.tetgame.modules.room.entity.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
    
    List<Room> findByHostId(UUID hostId);
    
    Page<Room> findByStatus(RoomStatus status, Pageable pageable);
    
    Page<Room> findByGameType(GameType gameType, Pageable pageable);
    
    Page<Room> findByGameTypeAndStatus(GameType gameType, RoomStatus status, Pageable pageable);
    
    @Query("SELECT r FROM Room r WHERE r.status IN ('WAITING', 'PLAYING')")
    List<Room> findActive();
    
    @Query("SELECT r FROM Room r WHERE r.status = 'FINISHED' AND r.updatedAt < :cutoffTime")
    List<Room> findFinishedRoomsBefore(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT COUNT(s) FROM RoomSeat s WHERE s.room.id = :roomId AND s.playerId IS NOT NULL")
    Integer countPlayers(@Param("roomId") UUID roomId);
    
    Optional<Room> findByIdAndStatus(UUID id, RoomStatus status);
}
