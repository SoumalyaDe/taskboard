package com.worldline.taskboard.model.entities;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

@Table("task_list")
@Builder(toBuilder = true)
public record TaskList(@Id
                       Long id,
                       String name,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt,
                       @Transient
                       List<Task> tasks
                       /*@MappedCollection(idColumn = "task_list_id")
                       List<Task> tasks*/) {

    // No-arg constructor for Spring Data JDBC
    public TaskList() {
        this(null, null, null, null, null);
    }

   /* public List<Task> tasks() {
        return Collections.unmodifiableList(tasks);
    }*/

    /*public TaskList addTask(Task task) {
        var newTasks = new ArrayList<>(tasks);
        newTasks.add(task);
        return new TaskList(id, name, newTasks);
    }

    public TaskList removeTask(Task task) {
        var newTasks = new ArrayList<>(tasks);
        newTasks.remove(task);
        return new TaskList(id, name, newTasks);
    }*/
}
