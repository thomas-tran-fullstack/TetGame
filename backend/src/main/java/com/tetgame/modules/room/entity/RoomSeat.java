package com.tetgame.modules.room.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "room_seats", indexes = {
    @Index(name = "idx_room_seats_room_id", columnList = "room_id"),
    @Index(name = "idx_room_seats_player_id", columnList = "player_id"),
    @Index(name = "idx_room_seats_position", columnList = "room_id, position")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomSeat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    
    @Column(nullable = false)
    private Integer position; // 0-3 for 4-player games
    
    @Column(name = "player_id")
    private UUID playerId;
    
    @Column(nullable = false)
    private Boolean isReady;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;
    
    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
        this.isReady = false;
    }
}
