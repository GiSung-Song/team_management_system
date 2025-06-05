package com.manager.taskmanager.task.entity;

import com.manager.taskmanager.common.BaseTimeEntity;
import com.manager.taskmanager.project.entity.Project;
import com.manager.taskmanager.projectmember.entity.ProjectMember;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "tasks")
public class Task extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_member_id", nullable = false)
    private ProjectMember projectMember;

    @Column(nullable = false, length = 100)
    private String taskName;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus taskStatus;

    private LocalDateTime deletedAt;

    public static Task createTask(ProjectMember projectMember, String taskName, String description,
                                  LocalDate startDate, LocalDate endDate, TaskStatus taskStatus) {
        return Task.builder()
                .projectMember(projectMember)
                .taskName(taskName)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .taskStatus(taskStatus)
                .build();
    }

    public void updateTask(String description, LocalDate startDate,
                           LocalDate endDate, TaskStatus taskStatus) {
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.taskStatus = taskStatus;
    }

    public void deleteTask() {
        this.deletedAt = LocalDateTime.now();
        this.taskStatus = TaskStatus.CANCELED;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
