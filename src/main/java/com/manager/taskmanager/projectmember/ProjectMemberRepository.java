package com.manager.taskmanager.projectmember;

import com.manager.taskmanager.projectmember.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
}