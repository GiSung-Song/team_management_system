package com.manager.taskmanager.project;

import com.manager.taskmanager.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
