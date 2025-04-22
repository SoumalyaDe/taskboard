package com.worldline.taskboard.model.dtos;

import com.worldline.taskboard.model.entities.Task;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record TaskDto(
        Long taskId,
        @NotNull
        TaskDetailsDto taskDetails) {

    public static Task toEntity(TaskDetailsDto taskDetailsDto) {
        return Task.builder()
                .name(taskDetailsDto.name())
                .description(taskDetailsDto.description())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static TaskDto of(Task entity) {
        var taskRequest = TaskDetailsDto.builder()
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
        return TaskDto.builder()
                .taskId(entity.getId())
                .taskDetails(taskRequest)
                .build();
    }
}
