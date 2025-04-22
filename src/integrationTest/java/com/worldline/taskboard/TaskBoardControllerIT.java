package com.worldline.taskboard;

import com.worldline.taskboard.repository.TaskListRepository;
import com.worldline.taskboard.repository.TaskRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

public class TaskBoardControllerIT extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TaskListRepository taskListRepository;

    @Autowired
    private TaskRepository taskRepository;


    @BeforeEach
    void setUp() {
        RestAssured.baseURI = getBaseUri();
        RestAssured.port = port;

        // Clean up before each test to ensure isolation
        taskRepository.deleteAll();
        taskListRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
        taskListRepository.deleteAll();
    }


    @Test
    void getAllLists_ShouldReturnEmptyList_WhenNoListsExist() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/lists")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("", Matchers.hasSize(0));
    }

    @Test
    void createList_ShouldCreateNewList_WhenValidNameProvided() {
        String testListName = "Test List";

        RestAssured.given()
                .contentType(ContentType.JSON)
                .queryParam("taskListName", testListName)
                .when()
                .post("/lists")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("name", Matchers.equalTo(testListName));

        // Verify list exists by getting all lists
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/lists")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("", Matchers.hasSize(1))
                .body("[0].name", Matchers.equalTo(testListName));
    }

    @Test
    void addTaskToList_ShouldAddTask_WhenListExists() {
        // First create a list
        String testListName = "Task List For Tasks";
        RestAssured.given()
                .contentType(ContentType.JSON)
                .queryParam("taskListName", testListName)
                .when()
                .post("/lists");

        // Then add a task to it
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Test Task",
                            "description": "This is a test task"
                        }
                        """)
                .when()
                .post("/lists/{listName}/tasks", testListName)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(Matchers.containsString("Task added to the task list=" + testListName));

        // Verify task was added
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/lists")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("find{it.name == '" + testListName + "'}.tasks", Matchers.hasSize(1))
                .body("find{it.name == '" + testListName + "'}.tasks[0].name", Matchers.equalTo("Test Task"));
    }

    @Test
    void updateTask_ShouldUpdateTaskDetails_WhenTaskExists() {
        // Create list and task first
        String testListName = "Update Task List";

        // Create list
        RestAssured.given()
                .contentType(ContentType.JSON)
                .queryParam("taskListName", testListName)
                .when()
                .post("/lists");

        // Add task
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Original Task",
                            "description": "Original description"
                        }
                        """)
                .when()
                .post("/lists/{listName}/tasks", testListName);

        // Get the task ID
        /*Long taskId = RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/lists")
                .then()
                .extract()
                .jsonPath()
                .getLong("find{it.name == '" + testListName + "'}.tasks[0].id");

        // Update the task
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "name": "Updated Task",
                    "description": "Updated description"
                }
                """)
                .when()
                .put("/tasks/{taskId}", taskId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(Matchers.containsString("Updated Task"));*/

        // Verify update
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/lists")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("find{it.name == '" + testListName + "'}.tasks[0].name", Matchers.equalTo("Updated Task"))
                .body("find{it.name == '" + testListName + "'}.tasks[0].description", Matchers.equalTo("Updated description"));
    }

    @Test
    void deleteTask_ShouldRemoveTask_WhenTaskExists() {
        // Create list and task first
        String testListName = "Delete Task List";

        // Create list
        RestAssured.given()
                .contentType(ContentType.JSON)
                .queryParam("taskListName", testListName)
                .when()
                .post("/lists");

        // Add task
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Task To Delete",
                            "description": "This task will be deleted"
                        }
                        """)
                .when()
                .post("/lists/{listName}/tasks", testListName);

        // Get the task ID
       /* Long taskId = RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/lists")
                .then()
                .extract()
                .jsonPath()
                .getLong("find{it.name == '" + testListName + "'}.tasks[0].id");*/

        // Delete the task
       /* RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/tasks/{taskId}", taskId)
                .then()
                .statusCode(HttpStatus.OK.value());*/

        // Verify task was deleted
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/lists")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("find{it.name == '" + testListName + "'}.tasks", Matchers.hasSize(0));
    }

    @Test
    void deleteList_ShouldRemoveList_WhenListExists() {
        // Create list first
        String testListName = "List To Delete";

        // Create list
      /*  Long listId = RestAssured.given()
                .contentType(ContentType.JSON)
                .queryParam("taskListName", testListName)
                .when()
                .post("/lists")
                .then()
                .extract()
                .jsonPath()
                .getLong("id");

        // Delete the list
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/lists/{listId}", listId)
                .then()
                .statusCode(HttpStatus.OK.value());*/

        // Verify list was deleted
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/lists")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("", Matchers.not(Matchers.hasItem(Matchers.hasEntry("name", testListName))));
    }

    @Test
    void moveTaskToList_ShouldMoveTask_WhenBothTaskAndTargetListExist() {
        // Create source list and task first
        String sourceListName = "Source List";
        String targetListName = "Target List";

        // Create source list
        RestAssured.given()
                .contentType(ContentType.JSON)
                .queryParam("taskListName", sourceListName)
                .when()
                .post("/lists");

        // Create target list
       /* Long targetListId = RestAssured.given()
                .contentType(ContentType.JSON)
                .queryParam("taskListName", targetListName)
                .when()
                .post("/lists")
                .then()
                .extract()
                .jsonPath()
                .getLong("id");

        // Add task to source list
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "name": "Task To Move",
                    "description": "This task will be moved"
                }
                """)
                .when()
                .post("/lists/{listName}/tasks", sourceListName);

        // Get the task ID
        Long taskId = RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/lists")
                .then()
                .extract()
                .jsonPath()
                .getLong("find{it.name == '" + sourceListName + "'}.tasks[0].id");

        // Move the task
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .put("/tasks/{taskId}/move/{listId}", taskId, targetListId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(Matchers.containsString("Task with id=" + taskId + " moved to task list with id=" + targetListId));
*/
        // Verify task was moved
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/lists")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("find{it.name == '" + sourceListName + "'}.tasks", Matchers.hasSize(0))
                .body("find{it.name == '" + targetListName + "'}.tasks", Matchers.hasSize(1))
                .body("find{it.name == '" + targetListName + "'}.tasks[0].name", Matchers.equalTo("Task To Move"));
    }

    @Test
    void createList_ShouldReturn400_WhenNameIsBlank() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .queryParam("taskListName", "")
                .when()
                .post("/lists")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void addTaskToList_ShouldReturn404_WhenListDoesNotExist() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Test Task",
                            "description": "This is a test task"
                        }
                        """)
                .when()
                .post("/lists/{listName}/tasks", "NonExistentList")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void updateTask_ShouldReturn404_WhenTaskDoesNotExist() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Updated Task",
                            "description": "Updated description"
                        }
                        """)
                .when()
                .put("/tasks/{taskId}", 999999)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }


}
