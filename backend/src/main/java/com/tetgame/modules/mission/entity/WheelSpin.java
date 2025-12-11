package com.tetgame.modules.mission.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "wheel_spins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WheelSpin {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private String prize;
    @Builder.Default
    private long amount = 0L;
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
