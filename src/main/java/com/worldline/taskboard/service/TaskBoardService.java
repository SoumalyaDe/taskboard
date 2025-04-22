package com.worldline.taskboard.service;

import com.worldline.taskboard.model.dtos.TaskDto;
import com.worldline.taskboard.model.dtos.TaskListDto;
import com.worldline.taskboard.model.dtos.TaskDetailsDto;

import java.util.List;

public interface TaskBoardService {
    List<TaskListDto> getAllLists();

    TaskListDto createList(String name);

    //TODO: Might be needed in future, so created in advance
    TaskDto createTask(TaskDetailsDto taskDetailsDto);

    void addTaskToList(String taskListName, TaskDetailsDto taskDetailsDto);

    void updateTask(Long taskId, TaskDetailsDto taskDetailsDto);

    void deleteTask(Long taskId);

    void deleteList(Long listId);

    void moveTaskToList(Long taskId, Long newListId);
}
