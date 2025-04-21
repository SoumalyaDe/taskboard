package com.worldline.taskboard.controller;

import com.worldline.taskboard.model.dtos.TaskDto;
import com.worldline.taskboard.model.dtos.TaskListDto;
import com.worldline.taskboard.model.dtos.TaskRequestDto;
import com.worldline.taskboard.service.TaskBoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/taskboard")
@RequiredArgsConstructor
public class TaskBoardController {
    private final TaskBoardService taskBoardService;

    @GetMapping("/lists")
    @Operation(summary = "Get all Task lists", description = "Retrieve all lists with their tasks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved lists")
    })
    public List<TaskListDto> getAllLists() {
        return taskBoardService.getAllLists();
    }

    @PostMapping("/lists")
    @Operation(summary = "Create a new Task list", description = "Create an empty Task list with a given name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task list created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Task list name already exists")
    })
    public ResponseEntity<TaskListDto> createList(@RequestParam @NotBlank String taskListName) {
        var taskList = taskBoardService.createList(taskListName);
        return new ResponseEntity<>(taskList, HttpStatus.CREATED);
    }

    @PostMapping("/lists/{listName}/tasks")
    @Operation(summary = "Add a Task to a list", description = "Add a new Task to an existing list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task added"),
            @ApiResponse(responseCode = "404", description = "Task list not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<String> addTaskToList(
            @PathVariable String listName,
            @Valid @RequestBody TaskRequestDto taskRequest
    ) {
        taskBoardService.addTaskToList(listName, taskRequest);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Task added to the task list=" + listName);
    }


    @PutMapping("/tasks/{taskId}")
    @Operation(summary = "Update a Task", description = "Update the name and description of a Task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskDto task) {
        var taskDto = taskBoardService.updateTask(taskId, task);
        return ResponseEntity.status(HttpStatus.OK)
                .body(taskDto);
    }

    @DeleteMapping("/tasks/{taskId}")
    @Operation(summary = "Delete a Task", description = "Delete a Task from a list")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task deleted"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskBoardService.deleteTask(taskId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/lists/{listId}")
    @Operation(summary = "Delete a Task list", description = "Delete a Task list and all its tasks")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task list deleted"),
            @ApiResponse(responseCode = "404", description = "Task list not found")
    })
    public ResponseEntity<Void> deleteList(@PathVariable Long listId) {
        taskBoardService.deleteList(listId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/tasks/{taskId}/move")
    @Operation(summary = "Move a Task to another list", description = "Move a Task from one list to another")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task moved"),
            @ApiResponse(responseCode = "404", description = "Task or target list not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<TaskDto> moveTaskToList(
            @PathVariable Long taskId,
            @PathVariable Long listId) {
        var TaskDto = taskBoardService.moveTaskToList(taskId, listId);
        return new ResponseEntity<>(TaskDto, HttpStatus.OK);
    }
}
