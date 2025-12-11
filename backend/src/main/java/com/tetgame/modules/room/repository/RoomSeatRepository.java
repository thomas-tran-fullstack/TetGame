package com.tetgame.modules.room.repository;

import com.tetgame.modules.room.entity.RoomSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomSeatRepository extends JpaRepository<RoomSeat, UUID> {
    
    List<RoomSeat> findByRoomId(UUID roomId);
    
    List<RoomSeat> findByRoomIdOrderByPosition(UUID roomId);
    
    Optional<RoomSeat> findByRoomIdAndPosition(UUID roomId, Integer position);
    
    Optional<RoomSeat> findByRoomIdAndPlayerId(UUID roomId, UUID playerId);
    
    @Query("SELECT s FROM RoomSeat s WHERE s.room.id = :roomId AND s.playerId IS NULL ORDER BY s.position LIMIT 1")
    Optional<RoomSeat> findFirstAvailableSeat(@Param("roomId") UUID roomId);
    
    @Query("SELECT COUNT(s) FROM RoomSeat s WHERE s.room.id = :roomId AND s.playerId IS NOT NULL")
    Integer countOccupiedSeats(@Param("roomId") UUID roomId);
    
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM RoomSeat s WHERE s.room.id = :roomId AND s.playerId = :playerId")
    Boolean playerInRoom(@Param("roomId") UUID roomId, @Param("playerId") UUID playerId);
}
