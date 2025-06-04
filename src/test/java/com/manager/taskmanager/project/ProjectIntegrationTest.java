package com.manager.taskmanager.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.taskmanager.config.DBContainerExtension;
import com.manager.taskmanager.config.TestDataFactory;
import com.manager.taskmanager.department.entity.Department;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.member.entity.Role;
import com.manager.taskmanager.project.dto.ProjectRegisterDto;
import com.manager.taskmanager.project.dto.ProjectUpdateDto;
import com.manager.taskmanager.project.entity.Project;
import com.manager.taskmanager.project.entity.ProjectStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@ExtendWith(DBContainerExtension.class)
@AutoConfigureMockMvc
public class ProjectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    private Department department;
    private Member leader;
    private Member member;
    private Project project;

    @BeforeEach
    void setUp() {
        department = testDataFactory.createDepartment();
        leader = testDataFactory.createLeader(department);
        member = testDataFactory.createMember(department);
        project = testDataFactory.createProject(leader, member);
    }

    @AfterEach
    void clearAuthentication() {
        testDataFactory.clearAuthentication();
    }

    @Nested
    @DisplayName("프로젝트 생성 테스트")
    class 프로젝트_생성_테스트 {

        @Test
        @DisplayName("프로젝트 생성 정상")
        void whenValidInput_thenProjectIsCreated() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            ProjectRegisterDto projectRegisterDto = new ProjectRegisterDto(
                    "team-project", "team-project description", LocalDate.now().plusDays(5),
                    LocalDate.now().plusMonths(5), ProjectStatus.PENDING.name()
            );

            mockMvc.perform(post("/api/project")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectRegisterDto)))
                    .andDo(print())
                    .andExpect(status().isCreated());

            Project findProject = findByProjectName(projectRegisterDto.getProjectName());

            assertThat(findProject).isNotNull();
            assertThat(findProject.getProjectName()).isEqualTo(projectRegisterDto.getProjectName());
            assertThat(findProject.getStartDate()).isEqualTo(projectRegisterDto.getStartDate());
            assertThat(findProject.getEndDate()).isEqualTo(projectRegisterDto.getEndDate());
            assertThat(findProject.getProjectMembers().size()).isEqualTo(1);
        }

        @Test
        @DisplayName("필수 입력 값 누락 시 400 반환")
        void whenMissingRequiredField_thenReturnBadRequest() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            ProjectRegisterDto projectRegisterDto = new ProjectRegisterDto(
                    null, "team-project description", LocalDate.now().plusDays(5),
                    LocalDate.now().plusMonths(5), ProjectStatus.PENDING.name()
            );

            mockMvc.perform(post("/api/project")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(projectRegisterDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비로그인 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            ProjectRegisterDto projectRegisterDto = new ProjectRegisterDto(
                    null, "team-project description", LocalDate.now().plusDays(5),
                    LocalDate.now().plusMonths(5), ProjectStatus.PENDING.name()
            );

            mockMvc.perform(post("/api/project")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(projectRegisterDto)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("직급이 낮을 시 403 반환")
        void whenLowPosition_thenReturnForbidden() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            ProjectRegisterDto projectRegisterDto = new ProjectRegisterDto(
                    "team-project", "team-project description", LocalDate.now().plusDays(5),
                    LocalDate.now().plusMonths(5), ProjectStatus.PENDING.name()
            );

            mockMvc.perform(post("/api/project")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(projectRegisterDto)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("프로젝트 수정 테스트")
    class 프로젝트_수정_테스트 {

        @Test
        @DisplayName("프로젝트 수정 정상")
        void whenValidInput_thenProjectIsUpdated() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            ProjectUpdateDto updateDto = new ProjectUpdateDto(
                    "update description", project.getStartDate(), project.getEndDate(), project.getProjectStatus().name()
            );

            mockMvc.perform(patch("/api/project/{projectId}", project.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andDo(print())
                    .andExpect(status().isOk());

            Project findProject = findByProjectName("test-project");

            assertThat(findProject.getDescription()).isEqualTo(updateDto.getDescription());
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenMissingRequiredField_thenReturnBadRequest() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            ProjectUpdateDto updateDto = new ProjectUpdateDto(
                    null, project.getStartDate(), project.getEndDate(), project.getProjectStatus().name()
            );

            mockMvc.perform(patch("/api/project/{projectId}", project.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비로그인 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            ProjectUpdateDto updateDto = new ProjectUpdateDto(
                    "update description", project.getStartDate(), project.getEndDate(), project.getProjectStatus().name()
            );

            mockMvc.perform(patch("/api/project/{projectId}", project.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("직급이 낮을 시 403 반환")
        void whenLowPosition_thenReturnForbidden() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            ProjectUpdateDto updateDto = new ProjectUpdateDto(
                    "update description", project.getStartDate(), project.getEndDate(), project.getProjectStatus().name()
            );

            mockMvc.perform(patch("/api/project/{projectId}", project.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("프로젝트 목록 조회 테스트")
    class 프로젝트_목록_조회_테스트 {

        @Test
        @DisplayName("프로젝트 목록 조회 정상")
        @WithMockUser(roles = "MEMBER")
        void whenValidInput_thenReturnProjectList() throws Exception {
            mockMvc.perform(get("/api/project")
                            .param("projectName", "test"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projectList[*].projectName", containsInAnyOrder("test-project")));
        }

        @Test
        @DisplayName("비로그인 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/project")
                            .param("projectName", "test"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("프로젝트 상세 조회 테스트")
    class 프로젝트_상세_조회_테스트 {

        @Test
        @DisplayName("프로젝트 상세 조회 정상")
        @WithMockUser(roles = "MEMBER")
        void whenValidInput_thenReturnProjectList() throws Exception {
            mockMvc.perform(get("/api/project/{projectId}", project.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projectName").value("test-project"));
        }

        @Test
        @DisplayName("잘못된 ID 입력 시 400 반환")
        @WithMockUser(roles = "MEMBER")
        void whenInvalidProjectID_thenReturnBadRequest() throws Exception {
            mockMvc.perform(get("/api/project/{projectId}", "projectID"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비로그인 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/project/{projectId}", project.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("프로젝트 없을 시 404 반환")
        @WithMockUser(roles = "MEMBER")
        void whenProjectNotFound_thenReturnNotfound() throws Exception {
            mockMvc.perform(get("/api/project/{projectId}", 43214321L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("프로젝트 삭제 테스트")
    class 프로젝트_삭제_테스트 {

        @Test
        @DisplayName("프로젝트 삭제 정상")
        void whenValidInput_thenReturnProjectList() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            mockMvc.perform(delete("/api/project/{projectId}", project.getId()))
                    .andDo(print())
                    .andExpect(status().isOk());

            Project findProject = findByProjectName("test-project");

            assertThat(findProject.getDeletedAt()).isNotNull();
            assertThat(findProject.getProjectStatus()).isEqualTo(ProjectStatus.CANCELED);
        }

        @Test
        @DisplayName("잘못된 ID 입력 시 400 반환")
        @WithMockUser(roles = "MEMBER")
        void whenInvalidProjectID_thenReturnBadRequest() throws Exception {
            mockMvc.perform(delete("/api/project/{projectId}", "projectID"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비로그인 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            mockMvc.perform(delete("/api/project/{projectId}", project.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("직급이 낮을 시 403 반환")
        void whenLowPosition_thenReturnForbidden() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            mockMvc.perform(delete("/api/project/{projectId}", project.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("프로젝트 없을 시 403 반환")
        void whenProjectNotFound_thenReturnForbidden() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            mockMvc.perform(delete("/api/project/{projectId}", 43214321L))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    private Project findByProjectName(String projectName) {
        return projectRepository.findAll().stream()
                .filter(m -> m.getProjectName().equals(projectName))
                .findFirst()
                .orElseThrow();
    }
}
