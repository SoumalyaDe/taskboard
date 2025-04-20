package com.worldline.taskboard.repository;

import com.worldline.taskboard.model.entities.Task;
import com.worldline.taskboard.model.entities.TaskList;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskListRepository extends CrudRepository<TaskList, Long> {

    Optional<TaskList> findByName(String name);

    /*@Query("SELECT task SET list_id = :newListId WHERE id = :taskId")
    List<Task> findTasksByListId(Long id);*/
}
