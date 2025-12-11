package com.tetgame.modules.match.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchRecord {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Builder.Default
    private OffsetDateTime startedAt = OffsetDateTime.now();
    private OffsetDateTime endedAt;

    @Column(name = "winner")
    private UUID winner;

    @Builder.Default
    private long pot = 0L;
}
