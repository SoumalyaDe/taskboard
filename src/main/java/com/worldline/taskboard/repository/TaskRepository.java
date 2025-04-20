package com.worldline.taskboard.repository;

import com.worldline.taskboard.model.entities.Task;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends CrudRepository<Task, Long> {

    /*@Query("UPDATE task SET list_id = :newListId WHERE id = :taskId")
    void updateTaskListId(@Param("taskId") Long taskId,
                          @Param("newListId") Long newListId);*/

    List<Task> findByListId(Long list_id);
}
