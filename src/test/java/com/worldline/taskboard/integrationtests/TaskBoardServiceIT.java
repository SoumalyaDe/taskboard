package com.worldline.taskboard.integrationtests;

import com.worldline.taskboard.exceptions.DuplicateEntityException;
import com.worldline.taskboard.model.dtos.TaskDto;
import com.worldline.taskboard.model.dtos.TaskRequestDto;
import com.worldline.taskboard.repository.TaskListRepository;
import com.worldline.taskboard.repository.TaskRepository;
import com.worldline.taskboard.service.TaskBoardService;
import com.worldline.taskboard.service.TaskBoardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.worldline.taskboard.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

//@Disabled
public class TaskBoardServiceIT extends BaseIntegrationTest {

//    @Container
//    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TaskListRepository taskListRepository;

    @Autowired
    private TaskRepository taskRepository;

    private TaskBoardService taskBoardService;

//    @BeforeAll
//    static void beforeAll() {
//        postgres.start();
//    }

    /*@DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.change-log", () -> "classpath:db/db.changelog-master.yaml");
    }
*/
    @BeforeEach
    void setUp() {
        taskBoardService = new TaskBoardServiceImpl(taskListRepository, taskRepository);
        taskListRepository.deleteAll();
        taskRepository.deleteAll();
    }

    @Test
    void testFullWorkflow() {
        // Create lists
        var workList = taskBoardService.createList(TASK_LIST_WORK);
        var personalList = taskBoardService.createList(TASK_LIST_PERSONAL);

        // View all lists
        var lists = taskBoardService.getAllLists();
        assertEquals(2, lists.size());
        assertEquals(TASK_LIST_WORK, lists.getFirst().getName());
        assertEquals(TASK_LIST_PERSONAL, lists.get(1).getName());

        // Add tasks to a list
        var taskRequest1 = TaskRequestDto.builder()
                .name("Call client")
                .description("Discuss the new project")
                .build();
        var task1 = TaskDto.builder()
                .taskRequest(taskRequest1)
                .build();
        var taskRequest2 = TaskRequestDto.builder()
                .name("Send email")
                .description("Send email to all stakeholders")
                .build();
        var task2 = TaskDto.builder()
                .taskRequest(taskRequest2)
                .build();
        taskBoardService.addTaskToList(TASK_LIST_WORK, taskRequest1);
        taskBoardService.addTaskToList(TASK_LIST_WORK, taskRequest2);

        lists = taskBoardService.getAllLists();
        assertEquals(2, lists.getFirst().getTasks().size());
        assertEquals("Call client", lists.getFirst().getTasks().getFirst().taskRequest().name());
        assertEquals("Call client", lists.getFirst().getTasks().get(1).taskRequest().name());
        assertTrue(lists.get(1).getTasks().isEmpty());

        // Update a task
        var newTaskRequest = TaskRequestDto.builder()
                .name("Meet client")
                .description("Discuss and finalize the new project")
                .build();
        var newTask = TaskDto.builder()
                .taskRequest(newTaskRequest)
                .build();
        var updatedTask = taskBoardService.updateTask(task1.taskId(), newTask);
        assertEquals("Call client updated", updatedTask.taskRequest().name());
        assertEquals("Discuss project updates and timeline", updatedTask.taskRequest().description());

        // Delete a task
        taskBoardService.deleteTask(task2.taskId());
        lists = taskBoardService.getAllLists();
        assertEquals(1, lists.getFirst().getTasks().size());
        assertEquals("Finish report", lists.getFirst().getTasks().getFirst().taskRequest().name());

        // Move task to another list
       /* taskBoardService.moveTaskToList(task1.id(), personalList.id());
        lists = taskBoardService.getAllLists();
        assertTrue(lists.getFirst().tasks().isEmpty());
        assertEquals(1, lists.get(1).tasks().size());
        assertEquals("Finish report", lists.get(1).tasks().getFirst().name());

        // Delete a list
        taskBoardService.deleteList(workList.id());
        lists = taskBoardService.getAllLists();
        assertEquals(1, lists.size());
        assertEquals(TASK_LIST_PERSONAL, lists.getFirst().name());*/
    }

    @Test
    void createList_shouldThrowExceptionOnDuplicateName() {
        taskBoardService.createList(TASK_LIST_WORK);

        var exception = assertThrows(
                DuplicateEntityException.class,
                () -> taskBoardService.createList(TASK_LIST_WORK)
        );

        assertEquals("A task list with name 'Work' already exists.", exception.getMessage());
    }

    @Test
    void addTaskToList_shouldThrowExceptionOnDuplicateTaskName() {
        var taskList = taskBoardService.createList(TASK_LIST_PERSONAL);
        var taskRequest1 = TaskRequestDto.builder()
                .name(TASK_NAME_TEST)
                .description(TASK_DESCRIPTION_TEST)
                .build();
        var task = TaskDto.builder()
                .taskRequest(taskRequest1)
                .build();
        taskBoardService.addTaskToList(TASK_LIST_PERSONAL, taskRequest1);
        var taskRequest2 = TaskRequestDto.builder()
                .name(TASK_NAME_TEST)
                .description(TASK_DESCRIPTION_ANOTHER)
                .build();
        var anotherTask = TaskDto.builder()
                .taskRequest(taskRequest2)
                .build();

        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskBoardService.addTaskToList(taskList.getName(), taskRequest2));

        assertEquals("A task with name 'Personal Task' already exists for the task list 'Personal'.", exception.getMessage());
    }

   /* @Test
    void addTaskToList_shouldThrowExceptionWhenListNotFound() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskBoardService.addTaskToList(999L, "Task", "Description")
        );

        assertEquals("Task list not found with id: 999", exception.getMessage());
    }

    @Test
    void updateTask_shouldThrowExceptionOnDuplicateTaskName() {
        TaskList list = taskBoardService.createList(TASK_LIST_WORK);
        Task task1 = taskBoardService.addTaskToList(list.id(), "Task 1", "Description 1");
        taskBoardService.addTaskToList(list.id(), "Task 2", "Description 2");

        UniqueNameViolationException exception = assertThrows(
                UniqueNameViolationException.class,
                () -> taskBoardService.updateTask(task1.id(), "Task 2", "Updated Description")
        );

        assertEquals("A task with name 'Task 2' already exists.", exception.getMessage());
    }

    @Test
    void moveTaskToList_shouldThrowExceptionOnDuplicateTaskName() {
        TaskList list1 = taskBoardService.createList(TASK_LIST_WORK);
        TaskList list2 = taskBoardService.createList(TASK_LIST_PERSONAL);

        Task task = taskBoardService.addTaskToList(list1.id(), "Task", "Description");
        taskBoardService.addTaskToList(list2.id(), "Task", "Another Description");

        UniqueNameViolationException exception = assertThrows(
                UniqueNameViolationException.class,
                () -> taskBoardService.moveTaskToList(task.id(), list2.id())
        );

        assertEquals("A task with name 'Task' already exists.", exception.getMessage());
    }*/
}

