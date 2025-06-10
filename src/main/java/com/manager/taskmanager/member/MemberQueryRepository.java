package com.manager.taskmanager.member;

import com.manager.taskmanager.member.dto.MemberTaskCountDto;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.member.entity.QMember;
import com.manager.taskmanager.projectmember.entity.QProjectMember;
import com.manager.taskmanager.task.entity.QTask;
import com.manager.taskmanager.task.entity.TaskStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Repository
public class MemberQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public MemberQueryRepository(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<Member> getAllMemberList(String departmentName, String name, boolean isManager) {
        QMember member = QMember.member;

        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(departmentName)) {
            builder.and(member.department.departmentName.containsIgnoreCase(departmentName));
        }

        if (StringUtils.hasText(name)) {
            builder.and(member.name.containsIgnoreCase(name));
        }

        if (!isManager) {
            builder.and(member.deletedAt.isNull());
        }

        return jpaQueryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    public List<MemberTaskCountDto> getRemainingTaskCountByMember(LocalDate date) {
        QTask task = QTask.task;
        QMember member = QMember.member;
        QProjectMember projectMember = QProjectMember.projectMember;

        return jpaQueryFactory
                .select(Projections.constructor(
                        MemberTaskCountDto.class,
                        task.projectMember.member.id,
                        task.count()
                ))
                .from(task)
                .join(task.projectMember, projectMember)
                .join(projectMember.member, member)
                .where(task.taskStatus.ne(TaskStatus.CANCELED)
                        .and(task.taskStatus.ne(TaskStatus.COMPLETED))
                        .and(task.endDate.eq(date))
                        .and(member.deletedAt.isNull()))
                .groupBy(task.projectMember.member.id)
                .fetch();
    }
}