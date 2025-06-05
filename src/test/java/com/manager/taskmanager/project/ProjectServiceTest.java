package com.manager.taskmanager.project;

import com.manager.taskmanager.common.CustomException;
import com.manager.taskmanager.common.ErrorCode;
import com.manager.taskmanager.config.DBContainerExtension;
import com.manager.taskmanager.department.DepartmentRepository;
import com.manager.taskmanager.department.entity.Department;
import com.manager.taskmanager.member.MemberRepository;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.member.entity.Position;
import com.manager.taskmanager.project.dto.*;
import com.manager.taskmanager.project.entity.Project;
import com.manager.taskmanager.project.entity.ProjectStatus;
import com.manager.taskmanager.projectmember.entity.ProjectMember;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@ExtendWith(DBContainerExtension.class)
class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

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
    @DisplayName("프로젝트 생성 Service")
    class 프로젝트_생성_서비스_테스트 {

        @Test
        @DisplayName("프로젝트 정상 생성")
        void whenValidInput_thenProjectIsCreated() {
            ProjectRegisterDto projectRegisterDto = new ProjectRegisterDto(
                    "taxi-project", "taxi application", LocalDate.now().plusDays(5),
                    LocalDate.now().plusWeeks(10), ProjectStatus.PENDING.name()
            );

            projectService.createProject(leader.getId(), projectRegisterDto);

            Project findProject = findByProjectName("taxi-project");

            assertThat(findProject.getProjectMembers().size()).isEqualTo(1);
            assertThat(findProject.getStartDate()).isEqualTo(projectRegisterDto.getStartDate());
            assertThat(findProject.getEndDate()).isEqualTo(projectRegisterDto.getEndDate());
        }

        @Test
        @DisplayName("프로젝트 생성 권한(직급) 없을 시 403 반환")
        void whenInvalidPosition_thenReturnForbidden() {
            ProjectRegisterDto projectRegisterDto = new ProjectRegisterDto(
                    "taxi-project", "taxi application", LocalDate.now().plusDays(5),
                    LocalDate.now().plusWeeks(10), ProjectStatus.PENDING.name()
            );

            assertThatThrownBy(() -> projectService.createProject(member.getId(), projectRegisterDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_PERMISSION);
                    });
        }

        @Test
        @DisplayName("회원 없을 시 404 반환")
        void whenMemberNotFound_thenReturnNotFound() {
            ProjectRegisterDto projectRegisterDto = new ProjectRegisterDto(
                    "taxi-project", "taxi application", LocalDate.now().plusDays(5),
                    LocalDate.now().plusWeeks(10), ProjectStatus.PENDING.name()
            );

            assertThatThrownBy(() -> projectService.createProject(43214321L, projectRegisterDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("프로젝트 수정 Service")
    class 프로젝트_수정_서비스_테스트 {

        @Test
        @DisplayName("프로젝트 정상 수정")
        void whenValidInput_thenProjectModified() {
            ProjectUpdateDto projectUpdateDto = new ProjectUpdateDto(
                    "modify taxi description", LocalDate.now().plusDays(1),
                    LocalDate.now().plusWeeks(10), ProjectStatus.PENDING.name()
            );

            projectService.updateProject(
                    leader.getId(), project.getId(), projectUpdateDto
            );

            Project findProject = findByProjectName("project");

            assertThat(findProject.getDescription()).isEqualTo(projectUpdateDto.getDescription());
            assertThat(findProject.getStartDate()).isEqualTo(projectUpdateDto.getStartDate());
            assertThat(findProject.getEndDate()).isEqualTo(projectUpdateDto.getEndDate());
            assertThat(findProject.getProjectStatus().name()).isEqualTo(projectUpdateDto.getProjectStatus());
        }

        @Test
        @DisplayName("프로젝트 수정 권한 없을 시 403 반환")
        void whenUnauthorized_thenReturnForbidden() {
            ProjectUpdateDto projectUpdateDto = new ProjectUpdateDto(
                    "modify taxi description", LocalDate.now().plusDays(1),
                    LocalDate.now().plusWeeks(10), ProjectStatus.PENDING.name()
            );

            assertThatThrownBy(() -> projectService.updateProject(member.getId(), project.getId(), projectUpdateDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_PERMISSION);
                    });
        }
    }

    @Nested
    @DisplayName("프로젝트 목록 조회 Service")
    class 프로젝트_목록_조회_서비스_테스트 {

        @Test
        @DisplayName("프로젝트 목록 정상 조회 - 프로젝트 명")
        void whenInputProjectName_thenReturnProjectList() {
            ProjectSearchCondition condition = new ProjectSearchCondition("proje", null, null);

            ProjectListDto projectList = projectService.getProjectList(condition);

            assertThat(projectList.getProjectList())
                    .hasSize(1)
                    .extracting("projectName")
                    .containsExactlyInAnyOrder("project");
        }

        @Test
        @DisplayName("프로젝트 목록 정상 조회 - 프로젝트에 속한 멤버 이름")
        void whenInputProjectMemberName_thenReturnProjectList() {
            ProjectSearchCondition condition = new ProjectSearchCondition(null, "leader", null);
            ProjectListDto projectList = projectService.getProjectList(condition);

            assertThat(projectList.getProjectList())
                    .hasSize(1)
                    .extracting("projectName")
                    .containsExactlyInAnyOrder("project");
        }

        @Test
        @DisplayName("프로젝트 목록 정상 조회 - 멤버 이름, 프로젝트 상태")
        void whenInputProjectMemberNameAndProjectStatus_thenReturnProjectList() {
            ProjectSearchCondition condition = new ProjectSearchCondition(null, "leader", "PENDING");
            ProjectListDto projectList = projectService.getProjectList(condition);

            assertThat(projectList.getProjectList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("프로젝트 상세 조회 Service")
    class 프로젝트_상세_조회_서비스_테스트 {

        @Test
        @DisplayName("프로젝트 상세 조회 정상")
        void whenValidInput_thenReturnProjectDetail() {
            ProjectDetailDto projectDetail = projectService.getProjectDetail(project.getId());

            assertThat(projectDetail.getProjectName()).isEqualTo(project.getProjectName());
            assertThat(projectDetail.getProjectStatus()).isEqualTo(project.getProjectStatus().name());
            assertThat(projectDetail.getStartDate()).isEqualTo(project.getStartDate());
            assertThat(projectDetail.getEndDate()).isEqualTo(project.getEndDate());
            assertThat(projectDetail.getProjectMembers())
                    .hasSize(1)
                    .extracting("name")
                    .containsExactlyInAnyOrder("leader");
        }

        @Test
        @DisplayName("프로젝트 번호 없을 시 404 반환")
        void whenInvalidProjectId_thenReturnNotFound() {
            assertThatThrownBy(() -> projectService.getProjectDetail(43214321L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROJECT_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("프로젝트 삭제 Service")
    class 프로젝트_삭제_서비스_테스트 {

        @Test
        @DisplayName("프로젝트 정상 삭제(soft delete)")
        void whenValidInput_thenProjectIsCanceled() {
            projectService.deleteProject(leader.getId(), project.getId());

            Project findProject = findByProjectName("project");

            assertThat(findProject.getDeletedAt()).isNotNull();
            assertThat(findProject.getProjectStatus()).isEqualTo(ProjectStatus.CANCELED);
        }

        @Test
        @DisplayName("프로젝트 권한 없을 시 403 반환")
        void whenInvalidProjectRole_thenReturnForbidden() {
            assertThatThrownBy(() -> projectService.deleteProject(member.getId(), project.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_PERMISSION);
                    });
        }

        @Test
        @DisplayName("프로젝트 완료 혹은 취소상태일 시 404 반환")
        void whenProjectIsCompletedOrCanceled_thenReturnBadRequest() {
            project.updateProject(
                    project.getDescription(), project.getStartDate(), project.getEndDate(), ProjectStatus.COMPLETED
            );

            assertThatThrownBy(() -> projectService.deleteProject(leader.getId(), project.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROJECT_CANNOT_BE_DELETED);
                    });
        }
    }

    private Project findByProjectName(String projectName) {
        return projectRepository.findAll().stream()
                .filter(m -> m.getProjectName().equals(projectName))
                .findFirst()
                .orElseThrow();
    }

}