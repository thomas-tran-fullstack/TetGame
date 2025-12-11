package com.tetgame.modules.mission.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTask {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "task_key", nullable = false)
    private String taskKey;

    @Builder.Default
    private boolean completed = false;
    @Builder.Default
    private int progress = 0;

    private OffsetDateTime completedAt;
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
