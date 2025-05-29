package com.manager.taskmanager.project.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProject is a Querydsl query type for Project
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProject extends EntityPathBase<Project> {

    private static final long serialVersionUID = 394788343L;

    public static final QProject project = new QProject("project");

    public final com.manager.taskmanager.common.QBaseTimeEntity _super = new com.manager.taskmanager.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<com.manager.taskmanager.projectmember.entity.ProjectMember, com.manager.taskmanager.projectmember.entity.QProjectMember> projectMembers = this.<com.manager.taskmanager.projectmember.entity.ProjectMember, com.manager.taskmanager.projectmember.entity.QProjectMember>createList("projectMembers", com.manager.taskmanager.projectmember.entity.ProjectMember.class, com.manager.taskmanager.projectmember.entity.QProjectMember.class, PathInits.DIRECT2);

    public final StringPath projectName = createString("projectName");

    public final EnumPath<ProjectStatus> projectStatus = createEnum("projectStatus", ProjectStatus.class);

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final ListPath<com.manager.taskmanager.task.entity.Task, com.manager.taskmanager.task.entity.QTask> tasks = this.<com.manager.taskmanager.task.entity.Task, com.manager.taskmanager.task.entity.QTask>createList("tasks", com.manager.taskmanager.task.entity.Task.class, com.manager.taskmanager.task.entity.QTask.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QProject(String variable) {
        super(Project.class, forVariable(variable));
    }

    public QProject(Path<? extends Project> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProject(PathMetadata metadata) {
        super(Project.class, metadata);
    }

}

