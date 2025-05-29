package com.manager.taskmanager.projectmember;

import com.manager.taskmanager.projectmember.entity.ProjectMember;
import com.manager.taskmanager.projectmember.entity.ProjectMemberStatus;
import com.manager.taskmanager.projectmember.entity.QProjectMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectMemberQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public ProjectMemberQueryRepository(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public ProjectMember getProjectMember(Long memberId, Long projectId) {
        QProjectMember pm = QProjectMember.projectMember;

        return jpaQueryFactory
                .selectFrom(pm)
                .where(pm.project.id.eq(projectId),
                        pm.member.id.eq(memberId),
                        pm.projectMemberStatus.eq(ProjectMemberStatus.ACTIVE)
                )
                .fetchOne();
    }
}