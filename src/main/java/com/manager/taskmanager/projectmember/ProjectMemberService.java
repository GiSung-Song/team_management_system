package com.manager.taskmanager.projectmember;

import com.manager.taskmanager.common.CustomException;
import com.manager.taskmanager.common.ErrorCode;
import com.manager.taskmanager.member.MemberService;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.project.entity.Project;
import com.manager.taskmanager.projectmember.dto.ProjectMemberRegisterDto;
import com.manager.taskmanager.projectmember.dto.ProjectMemberUpdateDto;
import com.manager.taskmanager.projectmember.entity.ProjectMember;
import com.manager.taskmanager.projectmember.entity.ProjectRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.manager.taskmanager.projectmember.dto.ProjectMemberRegisterDto.ProjectMemberDto;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ProjectMemberQueryRepository projectMemberQueryRepository;
    private final MemberService memberService;

    // 프로젝트 멤버 추가
    @Transactional
    public void addProjectMember(Long memberId, Long projectId, ProjectMemberRegisterDto dto) {
        ProjectMember projectMember = getProjectMemberAndCheckLeader(memberId, projectId);
        Project project = projectMember.getProject();

        List<Long> memberIds = dto.getProjectMemberDtoList().stream()
                .map(ProjectMemberDto::getMemberId)
                .collect(Collectors.toList());

        List<Member> memberList = memberService.getAllActiveMember(memberIds);

        if (memberList.size() != memberIds.size()) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Map<Long, Member> memberMap = memberList.stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        Set<Long> projectMemberSet = project.getProjectMembers().stream()
                .map(pm -> pm.getMember().getId())
                .collect(Collectors.toSet());

        for (ProjectMemberDto projectMemberDto : dto.getProjectMemberDtoList()) {
            Member member = memberMap.get(projectMemberDto.getMemberId());

            if (member == null) {
                throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
            }

            if (projectMemberSet.contains(member.getId())) {
                continue;
            }

            LocalDate startDate = projectMemberDto.getStartDate();
            LocalDate endDate = projectMemberDto.getEndDate();

            checkProjectDate(startDate, endDate, project.getStartDate(), project.getEndDate());

            ProjectMember pm = ProjectMember.createMember(
                    member,
                    ProjectRole.valueOf(projectMemberDto.getProjectRole()),
                    startDate,
                    endDate
            );

            project.addProjectMember(pm);
        }
    }

    // 프로젝트 멤버 삭제(soft delete)
    @Transactional
    public void deleteProjectMember(Long loginId, Long projectId, Long memberId) {
        getProjectMemberAndCheckLeader(loginId, projectId);

        ProjectMember projectMember = projectMemberQueryRepository.getProjectMember(memberId, projectId);

        if (projectMember == null) {
            throw new CustomException(ErrorCode.PROJECT_MEMBER_CANNOT_BE_DELETED);
        }

        projectMember.deleteProjectMember();
    }

    // 프로젝트 멤버 정보 변경
    @Transactional
    public void updateProjectMember(Long loginId, Long projectId, Long memberId, ProjectMemberUpdateDto dto) {
        getProjectMemberAndCheckLeader(loginId, projectId);

        ProjectMember projectMember = projectMemberQueryRepository.getProjectMember(memberId, projectId);

        if (projectMember == null) {
            throw new CustomException(ErrorCode.PROJECT_MEMBER_NOT_FOUND);
        }

        Project project = projectMember.getProject();

        checkProjectDate(dto.getStartDate(), dto.getEndDate(), project.getStartDate(), project.getEndDate());

        projectMember.updateProjectMember(
                dto.getStartDate(), dto.getEndDate(), ProjectRole.valueOf(dto.getProjectRole())
        );
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
