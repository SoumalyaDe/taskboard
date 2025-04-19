package com.worldline.taskboard.service;

import com.worldline.taskboard.model.dtos.TaskDto;
import com.worldline.taskboard.model.dtos.TaskListDto;
import com.worldline.taskboard.model.entities.Task;
import com.worldline.taskboard.model.entities.TaskList;
import com.worldline.taskboard.repository.TaskListRepository;
import com.worldline.taskboard.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.worldline.taskboard.TestContstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void getAllLists_shouldReturnAllTaskListsWithTasks() {
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


}