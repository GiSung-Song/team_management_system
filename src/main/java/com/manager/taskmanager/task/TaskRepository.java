package com.manager.taskmanager.task;

import com.manager.taskmanager.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
