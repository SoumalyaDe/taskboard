package com.worldline.taskboard.model.entities;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("task")
@Builder(toBuilder = true)
public record Task(@Id
                   Long id,
                   Long listId,
                   String name,
                   String description,
                   LocalDateTime createdAt,
                   LocalDateTime updatedAt) {

    //no-arg constructor for Spring Data JDBC
    public Task() {
        this(null, null, null, null, null, null);
    }
}
