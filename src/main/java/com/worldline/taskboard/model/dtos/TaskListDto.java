package com.worldline.taskboard.model.dtos;

import com.worldline.taskboard.model.entities.TaskList;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class TaskListDto {
    private final Long listId;
    @NotBlank(message = "Task List name must not be blank")
    @Size(min = 1, max = 100, message = "Task List name must not exceed 100 characters")
    private final String name;
    private List<TaskDto> tasks;

    public static TaskList toEntity(TaskListDto dto) {
        return TaskList.builder()
                .name(dto.getName())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static TaskListDto of(TaskList entity) {
        return TaskListDto.builder()
                .listId(entity.id())
                .name(entity.name())
                .build();
    }
}
