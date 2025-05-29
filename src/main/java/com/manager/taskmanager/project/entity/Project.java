package com.manager.taskmanager.project.entity;

import com.manager.taskmanager.common.BaseTimeEntity;
import com.manager.taskmanager.projectmember.entity.ProjectMember;
import com.manager.taskmanager.task.entity.Task;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "projects")
@SQLDelete(sql = "UPDATE projects SET deleted_at = NOW(), project_status = 'CANCELED' WHERE id = ?")
public class Project extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String projectName;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus projectStatus;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    @Builder.Default
    private List<ProjectMember> projectMembers = new ArrayList<>();

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    private LocalDateTime deletedAt;

    public void addProjectMember(ProjectMember projectMember) {
        projectMember.setProject(this);
        projectMembers.add(projectMember);
    }

    public void updateProject(String description, LocalDate startDate, LocalDate endDate, ProjectStatus projectStatus) {
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.projectStatus = projectStatus;
    }
}
