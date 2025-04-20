package com.worldline.taskboard.service;

import com.worldline.taskboard.exceptions.DuplicateEntityException;
import com.worldline.taskboard.exceptions.EntityNotFoundException;
import com.worldline.taskboard.model.dtos.TaskDto;
import com.worldline.taskboard.model.dtos.TaskListDto;
import com.worldline.taskboard.model.entities.Task;
import com.worldline.taskboard.model.entities.TaskList;
import com.worldline.taskboard.repository.TaskListRepository;
import com.worldline.taskboard.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.worldline.taskboard.TestContstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskBoardServiceImplTest {
    @Mock
    private TaskListRepository taskListRepository;
    @Mock
    private TaskRepository taskRepository;
    @InjectMocks
    private TaskBoardServiceImpl taskBoardService;

    private TaskList taskList;
    private Task task;
    private TaskListDto listDto;
    private TaskDto taskDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        taskList = TaskList.builder()
                .id(TEST_TASK_LIST_ID)
                .name(TASK_LIST_PERSONAL)
                .createdAt(now)
                .build();

        task = Task.builder()
                .id(TEST_TASK_ID_1)
                .name(TASK_NAME_TEST)
                .description(TASK_DESCRIPTION_TEST)
                .listId(TEST_TASK_LIST_ID)
                .createdAt(now)
                .build();

        listDto = TaskListDto.builder()
                .listId(TEST_TASK_LIST_ID)
                .name(TASK_LIST_PERSONAL)
                .tasks(new ArrayList<>())
                .build();

        taskDto = TaskDto.builder()
                .taskId(TEST_TASK_ID_1)
                .name(TASK_NAME_TEST)
                .description(TASK_DESCRIPTION_TEST)
                .build();
    }

    @Test
    void getAllLists_shouldReturnAllLists() {
        var taskLists = List.of(taskList);
        var tasks = List.of(task);
        when(taskListRepository.findAll()).thenReturn(taskLists);
        when(taskListRepository.findByListId(taskList.id())).thenReturn(tasks);

        var result = taskBoardService.getAllLists();

        assertEquals(1, result.size());
        assertEquals(taskList.id(), result.getFirst().getListId());
        assertEquals(taskList.name(), result.getFirst().getName());
        assertEquals(1, result.getFirst().getTasks().size());
        assertEquals(task.id(), result.getFirst().getTasks().getFirst().taskId());
        verify(taskListRepository).findAll();
        verify(taskListRepository).findByListId(taskList.id());
    }

    @Test
    void createList_shouldSaveTaskList() {
        when(taskListRepository.save(any(TaskList.class))).thenReturn(taskList);

        var result = taskBoardService.createList(TASK_LIST_PERSONAL);

        assertNotNull(result);
        assertEquals(taskList.id(), result.getListId());
        assertEquals(TASK_LIST_PERSONAL, result.getName());
        verify(taskListRepository).save(any(TaskList.class));
    }

    @Test
    void createList_whenNameAlreadyExists_shouldThrowDuplicateEntityException() {
        when(taskListRepository.save(any(TaskList.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        assertThrows(DuplicateEntityException.class, () -> taskBoardService.createList(anyString()));
        verify(taskListRepository).save(any(TaskList.class));
    }

    @Test
    void createTask_shouldSaveTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        var result = taskBoardService.createTask(taskDto);

        assertNotNull(result);
        assertEquals(taskDto.taskId(), result.taskId());
        assertEquals(taskDto.name(), result.name());
        assertEquals(taskDto.description(), result.description());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void addTaskToList_shouldAddTaskToList() {
        var inputTask = TaskDto.builder()
                .name(TASK_NAME_OTHER)
                .description(TASK_DESCRIPTION_TEST)
                .build();
        when(taskListRepository.findByName(TASK_LIST_PERSONAL)).thenReturn(Optional.of(taskList));
        when(taskListRepository.findByListId(taskList.id())).thenReturn(List.of(task));

        taskBoardService.addTaskToList(TASK_LIST_PERSONAL, inputTask);

        verify(taskListRepository).findByName(TASK_LIST_PERSONAL);
        verify(taskListRepository).findByListId(taskList.id());
        verify(taskRepository).save(any(Task.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void addTaskToList_whenListNameIsBlank_shouldThrowIllegalArgumentException(String listName) {
        assertThrows(IllegalArgumentException.class, () -> taskBoardService.addTaskToList(listName, taskDto));
    }

    @Test
    void addTaskToList_whenListNotFound_shouldThrowEntityNotFoundException() {
        var listName = "Non-existent List";

        when(taskListRepository.findByName(listName)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                taskBoardService.addTaskToList(listName, taskDto));

        verify(taskListRepository).findByName(listName);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void addTaskToList_whenTaskWithSameNameExists_shouldThrowIllegalArgumentException() {
        when(taskListRepository.findByName(TASK_LIST_PERSONAL)).thenReturn(Optional.of(taskList));
        when(taskListRepository.findByListId(taskList.id())).thenReturn(List.of(task));

        assertThrows(IllegalArgumentException.class, () -> taskBoardService.addTaskToList(TASK_LIST_PERSONAL, taskDto));

        verify(taskListRepository).findByName(TASK_LIST_PERSONAL);
        verify(taskListRepository).findByListId(taskList.id());
        verify(taskRepository, never()).save(any(Task.class));
    }

    //TODO: tests for update task

    @Test
    void shouldDeleteTask() {
        when(taskRepository.findById(TEST_TASK_ID_1)).thenReturn(Optional.of(task));

        taskBoardService.deleteTask(TEST_TASK_ID_1);

        verify(taskRepository).findById(TEST_TASK_ID_1);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_whenTaskNotFound_shouldThrowEntityNotFoundException() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskBoardService.deleteTask(TEST_TASK_ID_1));

        verify(taskRepository).findById(TEST_TASK_ID_1);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void shouldDeleteTaskList() {
        when(taskListRepository.findById(taskList.id())).thenReturn(Optional.of(taskList));

        taskBoardService.deleteList(TEST_TASK_LIST_ID);

        verify(taskListRepository).findById(TEST_TASK_LIST_ID);
        verify(taskListRepository).delete(taskList);
    }

    @Test
    void deleteList_whenListNotFound_shouldThrowEntityNotFoundException() {
        when(taskListRepository.findById(TEST_TASK_LIST_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskBoardService.deleteList(TEST_TASK_LIST_ID));

        verify(taskListRepository).findById(TEST_TASK_LIST_ID);
        verify(taskListRepository, never()).delete(any(TaskList.class));
    }

    //TODO: tests for moveTasktoList
}
