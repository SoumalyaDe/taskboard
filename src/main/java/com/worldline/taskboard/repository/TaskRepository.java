package com.worldline.taskboard.repository;

import com.worldline.taskboard.model.entities.Task;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends CrudRepository<Task, Long> {
    List<Task> findByListId(Long listId);
}
