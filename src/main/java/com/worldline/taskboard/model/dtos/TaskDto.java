package com.worldline.taskboard.model.dtos;

import com.worldline.taskboard.model.entities.Task;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TaskDto(
        Long taskId,
        TaskRequestDto requestDto) {

    public static Task toEntity(TaskRequestDto taskRequestDto) {
        return Task.builder()
                .name(taskRequestDto.name())
                .description(taskRequestDto.description())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static TaskDto of(Task entity) {
        var taskRequest = TaskRequestDto.builder()
                .name(entity.name())
                .description(entity.description())
                .build();
        return TaskDto.builder()
                .taskId(entity.id())
                .requestDto(taskRequest)
                .build();
    }
}
