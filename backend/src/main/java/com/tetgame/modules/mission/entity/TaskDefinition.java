package com.tetgame.modules.mission.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDefinition {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(name = "task_key", unique = true, nullable = false)
    private String taskKey;

    private String title;
    private String description;
    private long reward;
    @Builder.Default
    private boolean repeatDaily = true;
}
