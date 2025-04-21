package com.worldline.taskboard.service;

import com.worldline.taskboard.exceptions.DuplicateEntityException;
import com.worldline.taskboard.exceptions.EntityNotFoundException;
import com.worldline.taskboard.model.dtos.TaskListDto;
import com.worldline.taskboard.model.dtos.TaskRequestDto;
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

import static com.worldline.taskboard.TestConstants.*;
import static com.worldline.taskboard.service.TaskBoardServiceImpl.TASK_LIST_NOT_FOUND_MESSAGE;
import static com.worldline.taskboard.service.TaskBoardServiceImpl.TASK_NOT_FOUND_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;
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
    private TaskRequestDto taskRequestDto;
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

        taskRequestDto = TaskRequestDto.builder()
                .name(TASK_NAME_TEST)
                .description(TASK_DESCRIPTION_TEST)
                .build();
    }

    @Test
    void getAllLists_shouldReturnAllLists() {
        var taskLists = List.of(taskList);
        var tasks = List.of(task);
        when(taskListRepository.findAll()).thenReturn(taskLists);
        when(taskRepository.findByListId(taskList.id())).thenReturn(tasks);

        var result = taskBoardService.getAllLists();

        assertEquals(1, result.size());
        assertEquals(taskList.id(), result.getFirst().getListId());
        assertEquals(taskList.name(), result.getFirst().getName());
        assertEquals(1, result.getFirst().getTasks().size());
        assertEquals(task.getId(), result.getFirst().getTasks().getFirst().taskId());
        verify(taskListRepository).findAll();
        verify(taskRepository).findByListId(taskList.id());
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

        var result = taskBoardService.createTask(taskRequestDto);

        assertNotNull(result);
        assertEquals(TEST_TASK_ID_1, result.taskId());
        assertEquals(taskRequestDto.name(), result.taskRequest().name());
        assertEquals(taskRequestDto.description(), result.taskRequest().description());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void addTaskToList_shouldAddTaskToList() {
        var inputTask = TaskRequestDto.builder()
                .name(TASK_NAME_OTHER)
                .description(TASK_DESCRIPTION_TEST)
                .build();
        when(taskListRepository.findByName(TASK_LIST_PERSONAL)).thenReturn(Optional.of(taskList));
        when(taskRepository.findByListId(taskList.id())).thenReturn(List.of(task));

        taskBoardService.addTaskToList(TASK_LIST_PERSONAL, inputTask);

        verify(taskListRepository).findByName(TASK_LIST_PERSONAL);
        verify(taskRepository).findByListId(taskList.id());
        verify(taskRepository).save(any(Task.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void addTaskToList_whenListNameIsBlank_shouldThrowIllegalArgumentException(String listName) {
        assertThrows(IllegalArgumentException.class, () -> taskBoardService.addTaskToList(listName, taskRequestDto));
    }

    @Test
    void addTaskToList_whenListNotFound_shouldThrowEntityNotFoundException() {
        var listName = "Non-existent List";

        when(taskListRepository.findByName(listName)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                taskBoardService.addTaskToList(listName, taskRequestDto));

        verify(taskListRepository).findByName(listName);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void addTaskToList_whenTaskWithSameNameExists_shouldThrowIllegalArgumentException() {
        when(taskListRepository.findByName(TASK_LIST_PERSONAL)).thenReturn(Optional.of(taskList));
        when(taskRepository.findByListId(taskList.id())).thenReturn(List.of(task));

        assertThrows(IllegalArgumentException.class, () -> taskBoardService.addTaskToList(TASK_LIST_PERSONAL, taskRequestDto));

        verify(taskListRepository).findByName(TASK_LIST_PERSONAL);
        verify(taskRepository).findByListId(taskList.id());
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

    @Test
    void moveTaskToList_success() {
        var targetListId = 999L;
        when(taskRepository.findById(TEST_TASK_ID_1)).thenReturn(Optional.of(task));
        when(taskListRepository.existsById(targetListId)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        taskBoardService.moveTaskToList(TEST_TASK_ID_1, targetListId);

        assertEquals(targetListId, task.getListId());
        verify(taskRepository).findById(TEST_TASK_ID_1);
        verify(taskListRepository).existsById(targetListId);
        verify(taskRepository).save(task);
    }

    @Test
    void moveTaskToList_TaskNotFound_ThrowsEntityNotFoundException() {
        var nonExistentTaskId = 555L;
        var targetListId = 999L;
        when(taskRepository.findById(nonExistentTaskId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> taskBoardService.moveTaskToList(nonExistentTaskId, targetListId)
        );

        assertEquals(String.format(TASK_NOT_FOUND_MESSAGE, nonExistentTaskId), exception.getMessage());
        verify(taskRepository).findById(nonExistentTaskId);
        verify(taskListRepository, never()).existsById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void moveTaskToList_ListNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        Long nonExistentListId = 999L;

        when(taskRepository.findById(TEST_TASK_ID_1)).thenReturn(Optional.of(task));
        when(taskListRepository.existsById(nonExistentListId)).thenReturn(false);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> taskBoardService.moveTaskToList(TEST_TASK_ID_1, nonExistentListId)
        );

        assertEquals(String.format(TASK_LIST_NOT_FOUND_MESSAGE, nonExistentListId), exception.getMessage());
        verify(taskRepository, times(1)).findById(TEST_TASK_ID_1);
        verify(taskListRepository, times(1)).existsById(nonExistentListId);
        verify(taskRepository, never()).save(any(Task.class));
    }
}
