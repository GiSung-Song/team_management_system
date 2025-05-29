package com.manager.taskmanager.project;

import com.manager.taskmanager.department.entity.QDepartment;
import com.manager.taskmanager.member.entity.QMember;
import com.manager.taskmanager.project.dto.ProjectDetailDto;
import com.manager.taskmanager.project.entity.Project;
import com.manager.taskmanager.project.entity.ProjectStatus;
import com.manager.taskmanager.project.entity.QProject;
import com.manager.taskmanager.projectmember.entity.QProjectMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
public class ProjectQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public ProjectQueryRepository(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<Project> getProjectList(String projectName, String memberName, ProjectStatus projectStatus) {
        QProject project = QProject.project;

        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(projectName)) {
            builder.and(project.projectName.containsIgnoreCase(projectName));
        }

        if (StringUtils.hasText(memberName)) {
            builder.and(project.projectMembers.any().member.name.containsIgnoreCase(memberName));
        }

        if (projectStatus != null) {
            builder.and(project.projectStatus.eq(projectStatus));
        }

        return jpaQueryFactory
                .selectFrom(project)
                .where(builder)
                .fetch();
    }

    public ProjectDetailDto getProjectDetail(Long projectId) {
        QProject p = QProject.project;
        QProjectMember pm = QProjectMember.projectMember;
        QMember m = QMember.member;
        QDepartment dept = QDepartment.department;

        Project project = jpaQueryFactory
                .selectFrom(p)
                .where(p.id.eq(projectId))
                .fetchOne();

        List<ProjectDetailDto.MemberInfo> memberList = jpaQueryFactory
                .select(Projections.constructor(ProjectDetailDto.MemberInfo.class,
                        m.id,
                        m.name,
                        m.phoneNumber,
                        pm.startDate,
                        pm.endDate,
                        pm.projectMemberStatus,
                        pm.projectRole,
                        m.position,
                        dept.departmentName
                ))
                .from(pm)
                .join(pm.member, m)
                .join(m.department, dept)
                .where(pm.project.id.eq(projectId))
                .fetch();

        return new ProjectDetailDto(
                project.getProjectName(),
                project.getStartDate(),
                project.getEndDate(),
                project.getProjectStatus().name(),
                project.getDeletedAt(),
                memberList
        );
    }
}
