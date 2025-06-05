package com.manager.taskmanager.projectmember;

import com.manager.taskmanager.common.CustomException;
import com.manager.taskmanager.common.ErrorCode;
import com.manager.taskmanager.config.DBContainerExtension;
import com.manager.taskmanager.department.DepartmentRepository;
import com.manager.taskmanager.department.entity.Department;
import com.manager.taskmanager.member.MemberRepository;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.member.entity.Position;
import com.manager.taskmanager.project.ProjectRepository;
import com.manager.taskmanager.project.entity.Project;
import com.manager.taskmanager.project.entity.ProjectStatus;
import com.manager.taskmanager.projectmember.dto.ProjectMemberRegisterDto;
import com.manager.taskmanager.projectmember.dto.ProjectMemberUpdateDto;
import com.manager.taskmanager.projectmember.entity.ProjectMember;
import com.manager.taskmanager.projectmember.entity.ProjectMemberStatus;
import com.manager.taskmanager.projectmember.entity.ProjectRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@ExtendWith(DBContainerExtension.class)
class ProjectMemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private ProjectMemberService projectMemberService;

    Department department;
    Member member;
    Member leader;
    Project project;
    ProjectMember projectMember;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .departmentName("DV1")
                .build();

        departmentRepository.saveAndFlush(department);

        member = Member.createMember(
                "emp-0001", "password", "member",
                "member@email.com", "01012341234", Position.STAFF, department
        );

        leader = Member.createMember(
                "emp-1000", "password", "leader",
                "leader@email.com", "01056785678", Position.DIRECTOR, department
        );

        memberRepository.saveAllAndFlush(List.of(member, leader));

        project = Project.builder()
                .projectName("project")
                .description("description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(5))
                .projectStatus(ProjectStatus.PROGRESS)
                .build();

        projectMember = ProjectMember
                .createMember(
                        leader, ProjectRole.LEADER, LocalDate.now(), LocalDate.now().plusWeeks(5)
                );

        project.addProjectMember(projectMember);

        projectRepository.saveAndFlush(project);
    }

    @Nested
    @DisplayName("프로젝트 멤버 추가 Service")
    class 프로젝트_멤버_추가_서비스_테스트 {

        @Test
        @DisplayName("프로젝트 멤버 정상 추가")
        void whenValidInput_thenMemberIsAdded() {
            ProjectMemberRegisterDto dto = new ProjectMemberRegisterDto(
                    List.of(
                            new ProjectMemberRegisterDto.ProjectMemberDto(
                                    member.getId(), ProjectRole.MEMBER.name(),
                                    LocalDate.now().plusDays(5), LocalDate.now().plusWeeks(3)
                            )
                    )
            );

            projectMemberService.addProjectMember(leader.getId(), project.getId(), dto);

            assertThat(project.getProjectMembers())
                    .hasSize(2)
                    .extracting(pm -> pm.getMember().getName(), ProjectMember::getProjectRole)
                    .containsExactlyInAnyOrder(
                            tuple("leader", ProjectRole.LEADER),
                            tuple("member", ProjectRole.MEMBER)
                    );
        }

        @Test
        @DisplayName("프로젝트 멤버 추가 권한(직급) 없을 시 403 반환")
        void whenInvalidPosition_thenReturnForbidden() {
            ProjectMemberRegisterDto dto = new ProjectMemberRegisterDto(
                    List.of(
                            new ProjectMemberRegisterDto.ProjectMemberDto(
                                    member.getId(), ProjectRole.MEMBER.name(),
                                    LocalDate.now().plusDays(5), LocalDate.now().plusWeeks(3)
                            )
                    )
            );

            assertThatThrownBy(() -> projectMemberService.addProjectMember(member.getId(), project.getId(), dto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_PERMISSION);
                    });
        }
    }

    @Nested
    @DisplayName("프로젝트 멤버 삭제 Service")
    class 프로젝트_멤버_삭제_서비스_테스트 {

        @Test
        @DisplayName("프로젝트 멤버 정상 삭제(soft delete)")
        void whenValidInput_thenMemberIsDeleted() {
            ProjectMember projectAddMember = ProjectMember.createMember(
                    member, ProjectRole.MEMBER, LocalDate.now().plusDays(5), LocalDate.now().plusWeeks(3));

            project.addProjectMember(projectAddMember);

            projectMemberService.deleteProjectMember(
                    leader.getId(), project.getId(), member.getId()
            );

            ProjectMember findProjectMember
                    = findByProjectIdAndMemberId(project.getId(), member.getId());

            assertThat(findProjectMember.getProjectMemberStatus()).isEqualTo(ProjectMemberStatus.INACTIVE);
            assertThat(findProjectMember.getEndDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("프로젝트 멤버가 없을 시에 400 반환")
        void whenNotExistProjectMember_thenReturnBadRequest() {
            assertThatThrownBy(() -> projectMemberService.deleteProjectMember(leader.getId(), project.getId(), member.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROJECT_MEMBER_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("프로젝트 멤버 수정 Service")
    class 프로젝트_멤버_수정_서비스_테스트 {

        @Test
        @DisplayName("프로젝트 멤버 정상 수정")
        void whenValidInput_thenProjectMemberIsUpdated() {
            ProjectMember projectAddMember = ProjectMember.createMember(
                    member, ProjectRole.MEMBER, LocalDate.now().plusDays(5), LocalDate.now().plusWeeks(3));

            project.addProjectMember(projectAddMember);

            ProjectMemberUpdateDto projectMemberUpdateDto = new ProjectMemberUpdateDto(
                    ProjectRole.LEADER.name(), LocalDate.now().plusDays(2), LocalDate.now().plusWeeks(1)
            );

            projectMemberService.updateProjectMember(
                    leader.getId(), project.getId(), member.getId(), projectMemberUpdateDto
            );

            assertThat(projectAddMember.getProjectRole()).isEqualTo(ProjectRole.LEADER);
        }

        @Test
        @DisplayName("프로젝트 멤버 수정 권한 없을 시 403 반환")
        void whenUnauthorized_thenReturnForbidden() {
            ProjectMember projectAddMember = ProjectMember.createMember(
                    member, ProjectRole.MEMBER, LocalDate.now().plusDays(5), LocalDate.now().plusWeeks(3));

            project.addProjectMember(projectAddMember);

            ProjectMemberUpdateDto projectMemberUpdateDto = new ProjectMemberUpdateDto(
                    ProjectRole.LEADER.name(), LocalDate.now().plusDays(2), LocalDate.now().plusWeeks(1)
            );

            assertThatThrownBy(() -> projectMemberService.updateProjectMember(member.getId(), project.getId(), member.getId(), projectMemberUpdateDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_PERMISSION);
                    });
        }

        @Test
        @DisplayName("프로젝트 멤버가 없을 시에 404 반환")
        void whenNotExistProjectMember_thenReturnNotFound() {
            ProjectMemberUpdateDto projectMemberUpdateDto = new ProjectMemberUpdateDto(
                    ProjectRole.LEADER.name(), LocalDate.now().plusDays(2), LocalDate.now().plusWeeks(1)
            );

            assertThatThrownBy(() -> projectMemberService.updateProjectMember(leader.getId(), project.getId(), member.getId(), projectMemberUpdateDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROJECT_MEMBER_NOT_FOUND);
                    });
        }
    }

    private ProjectMember findByProjectIdAndMemberId(Long projectId, Long memberId) {
        return projectMemberRepository.findAll().stream()
                .filter(pm -> pm.getMember().getId().equals(memberId))
                .filter(pm -> pm.getProject().getId().equals(projectId))
                .findFirst()
                .orElseThrow();
    }

}