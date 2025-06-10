package com.manager.taskmanager.projectmember;

import com.manager.taskmanager.global.error.CustomException;
import com.manager.taskmanager.global.error.ErrorCode;
import com.manager.taskmanager.projectmember.entity.ProjectMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@RequiredArgsConstructor
@Component
public class ProjectMemberUtilService {

    private final ProjectMemberQueryRepository projectMemberQueryRepository;

    // 프로젝트 멤버 조회
    public ProjectMember getProjectMember(Long memberId, Long projectId) {
        ProjectMember projectMember = projectMemberQueryRepository.getProjectMember(memberId, projectId);

        if (projectMember == null) {
            throw new CustomException(ErrorCode.PROJECT_MEMBER_NOT_FOUND);
        }

        return projectMember;
    }

    // 프로젝트 권한 체크 및 조회
    public ProjectMember getProjectMemberAndCheckLeader(Long memberId, Long projectId) {
        ProjectMember projectMember = projectMemberQueryRepository.getProjectMember(memberId, projectId);

        if (projectMember == null || projectMember.getProjectRole().getLevel() < 3) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        return projectMember;
    }

    // 프로젝트 허용일 체크
    public void checkProjectDate(LocalDate startDate, LocalDate endDate, LocalDate projectStartDate, LocalDate projectEndDate) {
        if (startDate.isBefore(projectStartDate) || endDate.isAfter(projectEndDate)) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_DATE);
        }
    }
}
