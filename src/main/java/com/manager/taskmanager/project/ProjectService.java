package com.manager.taskmanager.project;

import com.manager.taskmanager.common.CustomException;
import com.manager.taskmanager.common.ErrorCode;
import com.manager.taskmanager.member.MemberService;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.project.dto.ProjectDetailDto;
import com.manager.taskmanager.project.dto.ProjectListDto;
import com.manager.taskmanager.project.dto.ProjectRegisterDto;
import com.manager.taskmanager.project.dto.ProjectUpdateDto;
import com.manager.taskmanager.project.entity.Project;
import com.manager.taskmanager.project.entity.ProjectStatus;
import com.manager.taskmanager.projectmember.ProjectMemberService;
import com.manager.taskmanager.projectmember.entity.ProjectMember;
import com.manager.taskmanager.projectmember.entity.ProjectRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectQueryRepository projectQueryRepository;
    private final ProjectMemberService projectMemberService;
    private final MemberService memberService;

    // 프로젝트 생성 및 담당자 할당
    @Transactional
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
    public void updateProject(Long memberId, Long projectId, ProjectUpdateDto dto) {
        ProjectMember projectMember = projectMemberService.getProjectMemberAndCheckLeader(memberId, projectId);

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
    public ProjectListDto getProjectList(String projectName, String memberName, String projectStatus) {
        ProjectStatus status = null;
        if (StringUtils.hasText(projectStatus)) {
            status = ProjectStatus.from(projectStatus);
        }

        List<Project> projectList = projectQueryRepository.getProjectList(projectName, memberName, status);

        List<ProjectListDto.ProjectInfo> projectInfo = projectList.stream()
                .map(m ->
                        new ProjectListDto.ProjectInfo(
                                m.getProjectName(), m.getStartDate(), m.getEndDate(),
                                m.getProjectStatus().name(), m.getDeletedAt()
                        ))
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
    public void deleteProject(Long memberId, Long projectId) {
        ProjectMember projectMember = projectMemberService
                .getProjectMemberAndCheckLeader(memberId, projectId);

        Project project = projectMember.getProject();

        if (project.getProjectStatus() == ProjectStatus.CANCELED || project.getProjectStatus() == ProjectStatus.COMPLETED) {
            throw new CustomException(ErrorCode.PROJECT_CANNOT_BE_DELETED);
        }

        projectRepository.delete(project);
    }
}
