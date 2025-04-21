package com.worldline.taskboard.model.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record TaskRequestDto(
    @NotBlank(message = "Task name must not be blank")
    @Size(min = 1, max = 100, message = "Task name must not exceed 100 characters")
    String name,
    @Size(max = 500, message = "Task description must not exceed 500 characters")
    String description) {
}
