package com.worldline.taskboard.model.entities;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("task_list")
@Builder(toBuilder = true)
public record TaskList(@Id
                       Long id,
                       String name,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {

    // No-arg constructor for Spring Data JDBC
    public TaskList() {
        this(null, null, null, null);
    }
}
