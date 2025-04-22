package com.worldline.taskboard.service;

import com.worldline.taskboard.exceptions.DuplicateEntityException;
import com.worldline.taskboard.exceptions.EntityNotFoundException;
import com.worldline.taskboard.model.dtos.TaskListDto;
import com.worldline.taskboard.model.dtos.TaskDetailsDto;
import com.worldline.taskboard.model.entities.Task;
import com.worldline.taskboard.model.entities.TaskList;
import com.worldline.taskboard.repository.TaskListRepository;
import com.worldline.taskboard.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
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
    private Task testTask1;
    private TaskListDto listDto;
    private TaskDetailsDto taskDetailsDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        taskList = TaskList.builder()
                .id(TEST_TASK_LIST_ID)
                .name(TASK_LIST_PERSONAL)
                .createdAt(now)
                .build();

        testTask1 = Task.builder()
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

        taskDetailsDto = TaskDetailsDto.builder()
                .name(TASK_NAME_TEST)
                .description(TASK_DESCRIPTION_TEST)
                .build();
    }

    @Test
    void getAllLists_shouldReturnAllLists() {
        var taskLists = List.of(taskList);
        var tasks = List.of(testTask1);
        when(taskListRepository.findAll()).thenReturn(taskLists);
        when(taskRepository.findByListId(taskList.id())).thenReturn(tasks);

        var result = taskBoardService.getAllLists();

        assertEquals(1, result.size());
        assertEquals(taskList.id(), result.getFirst().getListId());
        assertEquals(taskList.name(), result.getFirst().getName());
        assertEquals(1, result.getFirst().getTasks().size());
        assertEquals(testTask1.getId(), result.getFirst().getTasks().getFirst().taskId());
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
    void createList_whenNameAlreadyExists_throwsDuplicateEntityException() {
        when(taskListRepository.save(any(TaskList.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        assertThrows(DuplicateEntityException.class, () -> taskBoardService.createList(anyString()));
        verify(taskListRepository).save(any(TaskList.class));
    }

    @Test
    void createTask_shouldSaveTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(testTask1);

        var result = taskBoardService.createTask(taskDetailsDto);

        assertNotNull(result);
        assertEquals(TEST_TASK_ID_1, result.taskId());
        assertEquals(taskDetailsDto.name(), result.taskDetails().name());
        assertEquals(taskDetailsDto.description(), result.taskDetails().description());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void addTaskToList_shouldAddTaskToList() {
        var inputTask = TaskDetailsDto.builder()
                .name(TASK_NAME_OTHER)
                .description(TASK_DESCRIPTION_TEST)
                .build();
        when(taskListRepository.findByName(TASK_LIST_PERSONAL)).thenReturn(Optional.of(taskList));
        when(taskRepository.findByListId(taskList.id())).thenReturn(List.of(testTask1));

        taskBoardService.addTaskToList(TASK_LIST_PERSONAL, inputTask);

        verify(taskListRepository).findByName(TASK_LIST_PERSONAL);
        verify(taskRepository).findByListId(taskList.id());
        verify(taskRepository).save(any(Task.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void addTaskToList_whenListNameIsBlank_throwsIllegalArgumentException(String listName) {
        assertThrows(IllegalArgumentException.class, () -> taskBoardService.addTaskToList(listName, taskDetailsDto));
    }

    @Test
    void addTaskToList_whenListNotFound_shouldThrowEntityNotFoundException() {
        var listName = "Non-existent List";

        when(taskListRepository.findByName(listName)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                taskBoardService.addTaskToList(listName, taskDetailsDto));

        verify(taskListRepository).findByName(listName);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void addTaskToList_whenTaskWithSameNameExists_throwsIllegalArgumentException() {
        when(taskListRepository.findByName(TASK_LIST_PERSONAL)).thenReturn(Optional.of(taskList));
        when(taskRepository.findByListId(taskList.id())).thenReturn(List.of(testTask1));

        assertThrows(IllegalArgumentException.class, () -> taskBoardService.addTaskToList(TASK_LIST_PERSONAL, taskDetailsDto));

        verify(taskListRepository).findByName(TASK_LIST_PERSONAL);
        verify(taskRepository).findByListId(taskList.id());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_success() {
        var updateRequest = TaskDetailsDto.builder()
                .name(TASK_NAME_OTHER)
                .description(TASK_DESCRIPTION_ANOTHER)
                .build();
        when(taskRepository.findById(TEST_TASK_ID_1)).thenReturn(Optional.of(testTask1));

        taskBoardService.updateTask(TEST_TASK_ID_1, updateRequest);

        var taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        var savedTask = taskCaptor.getValue();
        assertEquals(TEST_TASK_ID_1, savedTask.getId());
        assertEquals(TASK_NAME_OTHER, savedTask.getName());
        assertEquals(TASK_DESCRIPTION_ANOTHER, savedTask.getDescription());
        assertEquals(TEST_TASK_LIST_ID, savedTask.getListId());
        assertEquals(now, savedTask.getCreatedAt());
    }

    @Test
    void updateTask_whenTaskNotFound_throwsEntityNotFoundException() {
        when(taskRepository.findById(TEST_TASK_ID_2)).thenReturn(Optional.empty());

        var exception = assertThrows(
                EntityNotFoundException.class,
                () -> taskBoardService.updateTask(TEST_TASK_ID_2, any(TaskDetailsDto.class))
        );

        assertEquals(String.format(TASK_NOT_FOUND_MESSAGE, TEST_TASK_ID_2), exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void shouldDeleteTaskSuccessfully() {
        when(taskRepository.findById(TEST_TASK_ID_1)).thenReturn(Optional.of(testTask1));

        taskBoardService.deleteTask(TEST_TASK_ID_1);

        verify(taskRepository).findById(TEST_TASK_ID_1);
        verify(taskRepository).delete(testTask1);
    }

    @Test
    void deleteTask_whenTaskNotFound_throwsEntityNotFoundException() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskBoardService.deleteTask(TEST_TASK_ID_1));

        verify(taskRepository).findById(TEST_TASK_ID_1);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void shouldDeleteTaskListSuccessfully() {
        when(taskListRepository.findById(taskList.id())).thenReturn(Optional.of(taskList));

        taskBoardService.deleteList(TEST_TASK_LIST_ID);

        verify(taskListRepository).findById(TEST_TASK_LIST_ID);
        verify(taskListRepository).delete(taskList);
    }

    @Test
    void deleteList_whenListNotFound_throwsEntityNotFoundException() {
        when(taskListRepository.findById(TEST_TASK_LIST_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskBoardService.deleteList(TEST_TASK_LIST_ID));

        verify(taskListRepository).findById(TEST_TASK_LIST_ID);
        verify(taskListRepository, never()).delete(any(TaskList.class));
    }

    @Test
    void moveTaskToList_success() {
        var targetListId = 999L;
        var updatedTask = Task.builder()
                .id(TEST_TASK_ID_1)
                .name(TASK_NAME_TEST)
                .description(TASK_DESCRIPTION_TEST)
                .listId(targetListId)
                .updatedAt(now)
                .build();

        when(taskRepository.findById(TEST_TASK_ID_1)).thenReturn(Optional.of(testTask1));
        when(taskListRepository.existsById(targetListId)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        taskBoardService.moveTaskToList(TEST_TASK_ID_1, targetListId);

        assertEquals(targetListId, updatedTask.getListId());
        verify(taskRepository).findById(TEST_TASK_ID_1);
        verify(taskListRepository).existsById(targetListId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void moveTaskToList_whenTaskNotFound_throwsEntityNotFoundException() {
        var nonExistentTaskId = 555L;
        var targetListId = 999L;
        when(taskRepository.findById(nonExistentTaskId)).thenReturn(Optional.empty());

        var exception = assertThrows(
                EntityNotFoundException.class,
                () -> taskBoardService.moveTaskToList(nonExistentTaskId, targetListId)
        );

        assertEquals(String.format(TASK_NOT_FOUND_MESSAGE, nonExistentTaskId), exception.getMessage());
        verify(taskRepository).findById(nonExistentTaskId);
        verify(taskListRepository, never()).existsById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void moveTaskToList_whenListNotFound_throwsEntityNotFoundException() {
        // Arrange
        Long nonExistentListId = 999L;

        when(taskRepository.findById(TEST_TASK_ID_1)).thenReturn(Optional.of(testTask1));
        when(taskListRepository.existsById(nonExistentListId)).thenReturn(false);

        // Act & Assert
        var exception = assertThrows(
                EntityNotFoundException.class,
                () -> taskBoardService.moveTaskToList(TEST_TASK_ID_1, nonExistentListId)
        );

        assertEquals(String.format(TASK_LIST_NOT_FOUND_MESSAGE, nonExistentListId), exception.getMessage());
        verify(taskRepository, times(1)).findById(TEST_TASK_ID_1);
        verify(taskListRepository, times(1)).existsById(nonExistentListId);
        verify(taskRepository, never()).save(any(Task.class));
    }
}
