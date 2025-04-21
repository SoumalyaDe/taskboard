package com.worldline.taskboard.model.dtos;

import com.worldline.taskboard.model.entities.Task;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record TaskDto(
        Long taskId,
        @NotNull
        TaskRequestDto taskRequest) {

    public static Task toEntity(TaskRequestDto taskRequestDto) {
        return Task.builder()
                .name(taskRequestDto.name())
                .description(taskRequestDto.description())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static TaskDto of(Task entity) {
        var taskRequest = TaskRequestDto.builder()
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
        return TaskDto.builder()
                .taskId(entity.getId())
                .taskRequest(taskRequest)
                .build();
    }
}
