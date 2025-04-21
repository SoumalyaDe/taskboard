package com.worldline.taskboard.service;

import com.worldline.taskboard.exceptions.DuplicateEntityException;
import com.worldline.taskboard.exceptions.EntityNotFoundException;
import com.worldline.taskboard.model.dtos.TaskDto;
import com.worldline.taskboard.model.dtos.TaskListDto;
import com.worldline.taskboard.model.dtos.TaskRequestDto;
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

    static final String TASK_NOT_FOUND_MESSAGE = "Task not found for id=%d";
    static final String TASK_LIST_NOT_FOUND_MESSAGE = "Task List not found with id=%d";

    private final TaskListRepository taskListRepository;
    private final TaskRepository taskRepository;

    @Override
    public List<TaskListDto> getAllLists() {
        var taskLists = new ArrayList<TaskList>();
        taskListRepository.findAll().forEach(taskLists::add);

        var taskListDtos = new ArrayList<TaskListDto>();
        taskLists.forEach(taskList -> {
            var taskListDto = TaskListDto.of(taskList);
            var taskDtos = taskRepository.findByListId(taskList.id())
                    .stream()
                    .map(TaskDto::of)
                    .toList();
            taskListDto.setTasks(taskDtos);
            taskListDtos.add(taskListDto);
        });

        return taskListDtos.isEmpty() ? List.of() : taskListDtos;
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
    public TaskDto createTask(TaskRequestDto taskRequestDto) {
        var taskEntity = TaskDto.toEntity(taskRequestDto)
                .toBuilder()
                .createdAt(LocalDateTime.now())
                .build();
        var savedEntity = taskRepository.save(taskEntity);
        return TaskDto.of(savedEntity);
    }

    @Override
    @Transactional
    public void addTaskToList(String taskListName, TaskRequestDto taskRequestDto) {
        if (Strings.isBlank(taskListName)) {
            throw new IllegalArgumentException("List name must not be blank!");
        }
        var taskListEntity = taskListRepository.findByName(taskListName)
                .orElseThrow(() ->
                        new EntityNotFoundException(String.format("Task list with name %s not found", taskListName)));
        if (taskRepository.findByListId(taskListEntity.id()).stream()
                .map(Task::getName)
                .anyMatch(taskRequestDto.name()::equals)) {
            throw new IllegalArgumentException(String.format("A task with name '%s' already exists for the task list '%s'.", taskRequestDto.name(), taskListName));
        } else {
            var taskEntity = TaskDto.toEntity(taskRequestDto)
                    .toBuilder()
                    .listId(taskListEntity.id())
                    .build();
            taskRepository.save(taskEntity);
        }
    }

    @Override
    @Transactional
    public TaskDto updateTask(Long taskId, TaskDto task) {
        return null;
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TASK_NOT_FOUND_MESSAGE, taskId)));
        taskRepository.delete(task);
    }


    @Override
    @Transactional
    public void deleteList(Long listId) {
        var taskList = taskListRepository.findById(listId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TASK_LIST_NOT_FOUND_MESSAGE, listId)));
        taskListRepository.delete(taskList);
    }

    @Override
    @Transactional
    public void moveTaskToList(Long taskId, Long newListId) {
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TASK_NOT_FOUND_MESSAGE, taskId)));
        if (!taskListRepository.existsById(newListId)) {
            throw new EntityNotFoundException(String.format(TASK_LIST_NOT_FOUND_MESSAGE, newListId));
        }
        task.setListId(newListId);
        taskRepository.save(task);
    }
}
