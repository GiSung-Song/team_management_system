package com.manager.taskmanager.project;

import com.manager.taskmanager.global.error.CustomException;
import com.manager.taskmanager.global.error.ErrorCode;
import com.manager.taskmanager.global.log.annotation.SaveLogging;
import com.manager.taskmanager.member.MemberService;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.project.dto.*;
import com.manager.taskmanager.project.entity.Project;
import com.manager.taskmanager.project.entity.ProjectStatus;
import com.manager.taskmanager.projectmember.ProjectMemberUtilService;
import com.manager.taskmanager.projectmember.entity.ProjectMember;
import com.manager.taskmanager.projectmember.entity.ProjectRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectQueryRepository projectQueryRepository;
    private final MemberService memberService;
    private final ProjectMemberUtilService pmUtilService;

    // 프로젝트 생성 및 담당자 할당
    @Transactional
    @SaveLogging(eventName = "프로젝트 생성")
    public void createProject(Long memberId, ProjectRegisterDto dto) {
        Member member = memberService.getActiveMemberById(memberId);

        if (member.getPosition().getLevel() < 5) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        Project project = Project.builder()
                .projectName(dto.getProjectName())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .projectStatus(ProjectStatus.valueOf(dto.getProjectStatus()))
                .build();

        ProjectMember projectLeader = ProjectMember.createMember(
                member, ProjectRole.LEADER, dto.getStartDate(), dto.getEndDate()
        );

        project.addProjectMember(projectLeader);

        projectRepository.save(project);
    }

    // 프로젝트 수정
    @Transactional
    @SaveLogging(eventName = "프로젝트 수정")
    public void updateProject(Long memberId, Long projectId, ProjectUpdateDto dto) {
        ProjectMember projectMember = pmUtilService.getProjectMemberAndCheckLeader(memberId, projectId);

        Project project = projectMember.getProject();

        project.updateProject(
                dto.getDescription(),
                dto.getStartDate(),
                dto.getEndDate(),
                ProjectStatus.valueOf(dto.getProjectStatus())
        );
    }

    // 프로젝트 목록 조회(조건 : 프로젝트명, 소속된 멤버 이름, 프로젝트 상태)
    @Transactional(readOnly = true)
    public ProjectListDto getProjectList(ProjectSearchCondition condition) {
        List<Project> projectList = projectQueryRepository.getProjectList(condition);

        List<ProjectListDto.ProjectInfo> projectInfo = projectList.stream()
                .map(project -> new ProjectListDto.ProjectInfo(
                        project.getProjectName(),
                        project.getStartDate(),
                        project.getEndDate(),
                        project.getProjectStatus().name(),
                        project.getDeletedAt()))
                .collect(Collectors.toList());

        return new ProjectListDto(projectInfo);
    }

    // 프로젝트 상세 조회
    @Transactional(readOnly = true)
    public ProjectDetailDto getProjectDetail(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
        }

        return projectQueryRepository.getProjectDetail(projectId);
    }

    // 프로젝트 삭제
    @Transactional
    @SaveLogging(eventName = "프로젝트 삭제")
    public void deleteProject(Long memberId, Long projectId) {
        ProjectMember projectMember = pmUtilService
                .getProjectMemberAndCheckLeader(memberId, projectId);

        Project project = projectMember.getProject();

        if (project.getProjectStatus() == ProjectStatus.CANCELED || project.getProjectStatus() == ProjectStatus.COMPLETED) {
            throw new CustomException(ErrorCode.PROJECT_CANNOT_BE_DELETED);
        }

        projectRepository.delete(project);
    }
}
