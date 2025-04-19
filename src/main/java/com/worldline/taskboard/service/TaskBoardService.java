package com.worldline.taskboard.service;

import com.worldline.taskboard.model.dtos.TaskDto;
import com.worldline.taskboard.model.dtos.TaskListDto;

import java.util.List;

public interface TaskBoardService {
    List<TaskListDto> getAllLists();

    TaskListDto createList(String name);

    TaskDto createTask(TaskDto task);

    void addTaskToList(String taskListName, TaskDto task);

    TaskDto updateTask(Long taskId, TaskDto task);

    void deleteTask(Long taskId);

    void deleteList(Long listId);

    TaskDto moveTaskToList(Long taskId, Long newListId);
}
