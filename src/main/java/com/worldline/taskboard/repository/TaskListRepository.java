package com.worldline.taskboard.repository;

import com.worldline.taskboard.model.entities.TaskList;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskListRepository extends CrudRepository<TaskList, Long> {
    Optional<TaskList> findByName(String name);
}
