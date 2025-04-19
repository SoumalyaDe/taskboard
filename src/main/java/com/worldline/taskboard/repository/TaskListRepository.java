package com.worldline.taskboard.repository;

import com.worldline.taskboard.model.entities.Task;
import com.worldline.taskboard.model.entities.TaskList;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskListRepository extends CrudRepository<TaskList, Long> {

    @Query("UPDATE task SET task_list_id = :newListId WHERE id = :taskId")
    void updateTaskListId(@Param("taskId") Long taskId,
                          @Param("newListId") Long newListId);

    Optional<TaskList> findByName(String name);

    List<Task> findByListId(Long listId);
}
