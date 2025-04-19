package com.worldline.taskboard.model.dtos;

import com.worldline.taskboard.model.entities.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TaskDto(
        Long taskId,
        @NotBlank(message = "Task name must not be blank")
        @Size(min = 1, max = 100, message = "Task name must not exceed 100 characters")
        String name,
        @Size(max = 500, message = "Task description must not exceed 500 characters")
        String description) {

    public static Task toEntity(TaskDto dto) {
        return Task.builder()
                .name(dto.name())
                .description(dto.description())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static TaskDto of(Task entity) {
        return TaskDto.builder()
                .taskId(entity.id())
                .name(entity.name())
                .description(entity.description())
                .build();
    }
}
