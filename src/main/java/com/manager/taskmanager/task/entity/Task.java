package com.manager.taskmanager.task.entity;

import com.manager.taskmanager.common.BaseTimeEntity;
import com.manager.taskmanager.project.entity.Project;
import com.manager.taskmanager.projectmember.entity.ProjectMember;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "tasks")
@SQLDelete(sql = "UPDATE tasks SET deleted_at = NOW() WHERE id = ?")
public class Task extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private ProjectMember member;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(nullable = false)
    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus taskStatus;

    private LocalDateTime deletedAt;
}
