package com.manager.taskmanager.projectmember.entity;

import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "project_members")
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole projectRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectMemberStatus projectMemberStatus;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    public static ProjectMember createMember(
            Member member, ProjectRole projectRole, LocalDate startDate, LocalDate endDate
    ) {
        return ProjectMember.builder()
                .member(member)
                .projectRole(projectRole)
                .projectMemberStatus(ProjectMemberStatus.ACTIVE)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void deleteProjectMember() {
        this.projectMemberStatus = ProjectMemberStatus.INACTIVE;
        this.endDate = LocalDate.now();
    }

    public void updateProjectMember(
            LocalDate startDate, LocalDate endDate, ProjectRole projectRole
    ) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.projectRole = projectRole;
    }

}
