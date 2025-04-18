package com.worldline.taskboard.service;

import com.worldline.taskboard.exceptions.DuplicateEntityException;
import com.worldline.taskboard.exceptions.EntityNotFoundException;
import com.worldline.taskboard.model.dtos.TaskDto;
import com.worldline.taskboard.model.dtos.TaskListDto;
import com.worldline.taskboard.model.entities.Task;
import com.worldline.taskboard.model.entities.TaskList;
import com.worldline.taskboard.repository.TaskListRepository;
import com.worldline.taskboard.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskBoardServiceImpl implements TaskBoardService {

    private final TaskListRepository taskListRepository;
    private final TaskRepository taskRepository;

    @Override
    public List<TaskListDto> getAllLists() {
        var taskLists = new ArrayList<TaskList>();
        taskListRepository.findAll().forEach(taskLists::add);

        var taskListDtos = new ArrayList<TaskListDto>();
        taskLists.forEach(taskList -> {
            var taskListDto = TaskListDto.of(taskList);
            var taskDtos = taskListRepository.findByListId(taskList.id())
                    .stream()
                    .map(TaskDto::of)
                    .toList();
            taskListDto.setTasks(taskDtos);
            taskListDtos.add(taskListDto);
        });

        return taskListDtos;
    }

    @Override
    @Transactional
    public TaskListDto createList(String name) {
        var taskListDto = TaskListDto.builder().name(name).build();
        var taskListEntity = TaskListDto.toEntity(taskListDto)
                .toBuilder()
                .createdAt(LocalDateTime.now())
                .build();
        try {
            taskListEntity = taskListRepository.save(taskListEntity);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEntityException("Task list name already exists!", e);
        }
        return TaskListDto.of(taskListEntity);
    }

    @Override
    @Transactional
    public TaskDto createTask(TaskDto taskDto) {
        var taskEntity = TaskDto.toEntity(taskDto)
                .toBuilder()
                .createdAt(LocalDateTime.now())
                .build();
        var savedEntity = taskRepository.save(taskEntity);
        return TaskDto.of(savedEntity);
    }

    @Override
    @Transactional
    public void addTaskToList(String taskListName, TaskDto taskDto) {
        if (Strings.isBlank(taskListName)) {
            throw new IllegalArgumentException("List name must not be blank!");
        }
        var taskListEntity = taskListRepository.findByName(taskListName)
                .orElseThrow(() ->
                        new EntityNotFoundException(String.format("Task list with name %s not found", taskListName)));
        if (taskListRepository.findByListId(taskListEntity.id()).stream().map(Task::name).anyMatch(taskDto.name()::equals)) {
            throw new IllegalArgumentException(String.format("A task with name '%s' already exists for the task list '%s'.", taskDto.name(), taskListName));
        } else {
            var taskEntity = TaskDto.toEntity(taskDto)
                    .toBuilder()
                    .listId(taskListEntity.id())
                    .build();
            taskRepository.save(taskEntity);
        }
    }

    @Override
    public TaskDto updateTask(Long taskId, TaskDto task) {
        return null;
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));
        taskRepository.delete(task);
    }


    @Override
    @Transactional
    public void deleteList(Long listId) {
        var taskList = taskListRepository.findById(listId)
                .orElseThrow(() -> new EntityNotFoundException("Task List not found with id: " + listId));
        taskListRepository.delete(taskList);
    }

    @Override
    public TaskDto moveTaskToList(Long taskId, Long newListId) {
        return null;
    }
}
