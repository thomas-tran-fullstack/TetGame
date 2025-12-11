package com.tetgame.modules.room.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rooms", indexes = {
    @Index(name = "idx_rooms_host_id", columnList = "host_id"),
    @Index(name = "idx_rooms_status", columnList = "status"),
    @Index(name = "idx_rooms_game_type", columnList = "game_type"),
    @Index(name = "idx_rooms_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameType gameType;
    
    @Column(nullable = false)
    private UUID hostId;
    
    @Column(nullable = false)
    private Integer maxPlayers;
    
    @Column(nullable = false)
    private Integer currentPlayers;

    @Enumerated(EnumType.STRING)
    @Column(name = "bet_level")
    private com.tetgame.modules.room.entity.BetLevel betLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status;
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomSeat> seats;
    
    @ElementCollection
    @CollectionTable(name = "room_spectators", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "spectator_id")
    private List<UUID> spectators;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.currentPlayers = 0;
        if (this.betLevel == null) this.betLevel = com.tetgame.modules.room.entity.BetLevel.BAN1;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
