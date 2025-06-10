package com.manager.taskmanager.task;

import com.manager.taskmanager.task.dto.TaskSearchCondition;
import com.manager.taskmanager.task.entity.QTask;
import com.manager.taskmanager.task.entity.Task;
import com.manager.taskmanager.task.entity.TaskStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TaskQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public TaskQueryRepository(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<Task> getTaskList(Long memberId, boolean isManager, TaskSearchCondition condition) {
        QTask task = QTask.task;

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (StringUtils.hasText(condition.getProjectName())) {
            booleanBuilder.and(task.project.projectName.containsIgnoreCase(condition.getProjectName()));
        }

        if (StringUtils.hasText(condition.getTaskName())) {
            booleanBuilder.and(task.taskName.containsIgnoreCase(condition.getTaskName()));
        }

        if (StringUtils.hasText(condition.getTaskStatus())) {
            booleanBuilder.and(task.taskStatus.eq(TaskStatus.valueOf(condition.getTaskStatus())));
        }

        if (!isManager && memberId != null) {
            booleanBuilder.and(task.projectMember.member.id.eq(memberId));
        }

        return jpaQueryFactory
                .selectFrom(task)
                .where(booleanBuilder)
                .orderBy(task.endDate.asc().nullsLast())
                .fetch();
    }

    public List<Long> getDeletedTaskAfter3Month(LocalDateTime date) {
        QTask task = QTask.task;

        return jpaQueryFactory
                .select(task.id)
                .from(task)
                .where(task.taskStatus.eq(TaskStatus.CANCELED)
                        .and(task.deletedAt.loe(date)))
                .fetch();
    }
}
