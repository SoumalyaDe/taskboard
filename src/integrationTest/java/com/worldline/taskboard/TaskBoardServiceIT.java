package com.worldline.taskboard;

import com.worldline.taskboard.exceptions.DuplicateEntityException;
import com.worldline.taskboard.model.dtos.TaskDetailsDto;
import com.worldline.taskboard.model.dtos.TaskDto;
import com.worldline.taskboard.repository.TaskListRepository;
import com.worldline.taskboard.repository.TaskRepository;
import com.worldline.taskboard.service.TaskBoardService;
import com.worldline.taskboard.service.TaskBoardServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TaskBoardServiceIT extends BaseIntegrationTest {


    @Autowired
    private TaskListRepository taskListRepository;

    @Autowired
    private TaskRepository taskRepository;

    private TaskBoardService taskBoardService;

    @BeforeEach
    void setUp() {
        taskBoardService = new TaskBoardServiceImpl(taskListRepository, taskRepository);
        taskListRepository.deleteAll();
        taskRepository.deleteAll();
    }

    @Test
    void testFullWorkflow() {
        // Create lists
        var workList = taskBoardService.createList(IntegrationTestConstants.TASK_LIST_WORK);
        var personalList = taskBoardService.createList(IntegrationTestConstants.TASK_LIST_PERSONAL);

        // View all lists
        var lists = taskBoardService.getAllLists();
        Assertions.assertEquals(2, lists.size());
        Assertions.assertEquals(IntegrationTestConstants.TASK_LIST_WORK, lists.getFirst().getName());
        Assertions.assertEquals(IntegrationTestConstants.TASK_LIST_PERSONAL, lists.get(1).getName());

        // Add tasks to a list
        var taskRequest1 = TaskDetailsDto.builder()
                .name("Call client")
                .description("Discuss the new project")
                .build();
        var task1 = TaskDto.builder()
                .taskDetails(taskRequest1)
                .build();
        var taskRequest2 = TaskDetailsDto.builder()
                .name("Send email")
                .description("Send email to all stakeholders")
                .build();
        var task2 = TaskDto.builder()
                .taskDetails(taskRequest2)
                .build();
        taskBoardService.addTaskToList(IntegrationTestConstants.TASK_LIST_WORK, taskRequest1);
        taskBoardService.addTaskToList(IntegrationTestConstants.TASK_LIST_WORK, taskRequest2);

        lists = taskBoardService.getAllLists();
        Assertions.assertEquals(2, lists.getFirst().getTasks().size());
        Assertions.assertEquals("Call client", lists.getFirst().getTasks().getFirst().taskDetails().name());
        Assertions.assertEquals("Call client", lists.getFirst().getTasks().get(1).taskDetails().name());
        Assertions.assertTrue(lists.get(1).getTasks().isEmpty());

        // Update a task
        var newTaskRequest = TaskDetailsDto.builder()
                .name("Meet client")
                .description("Discuss and finalize the new project")
                .build();
        taskBoardService.updateTask(task1.taskId(), newTaskRequest);
        /*assertEquals("Call client updated", newTaskRequest.name());
        assertEquals("Discuss project updates and timeline", newTaskRequest.description());*/

        // Delete a task
        taskBoardService.deleteTask(task2.taskId());
        lists = taskBoardService.getAllLists();
        Assertions.assertEquals(1, lists.getFirst().getTasks().size());
        Assertions.assertEquals("Finish report", lists.getFirst().getTasks().getFirst().taskDetails().name());

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
        taskBoardService.createList(IntegrationTestConstants.TASK_LIST_WORK);

        var exception = Assertions.assertThrows(
                DuplicateEntityException.class,
                () -> taskBoardService.createList(IntegrationTestConstants.TASK_LIST_WORK)
        );

        Assertions.assertEquals("A task list with name 'Work' already exists.", exception.getMessage());
    }

    @Test
    void addTaskToList_shouldThrowExceptionOnDuplicateTaskName() {
        var taskList = taskBoardService.createList(IntegrationTestConstants.TASK_LIST_PERSONAL);
        var taskRequest1 = TaskDetailsDto.builder()
                .name(IntegrationTestConstants.TASK_NAME_TEST)
                .description(IntegrationTestConstants.TASK_DESCRIPTION_TEST)
                .build();
        taskBoardService.addTaskToList(IntegrationTestConstants.TASK_LIST_PERSONAL, taskRequest1);
        var taskRequest2 = TaskDetailsDto.builder()
                .name(IntegrationTestConstants.TASK_NAME_TEST)
                .description(IntegrationTestConstants.TASK_DESCRIPTION_ANOTHER)
                .build();

        var exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> taskBoardService.addTaskToList(taskList.getName(), taskRequest2));

        Assertions.assertEquals("A task with name 'Personal Task' already exists for the task list 'Personal'.", exception.getMessage());
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

