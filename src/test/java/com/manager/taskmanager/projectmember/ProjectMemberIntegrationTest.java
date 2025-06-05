package com.manager.taskmanager.projectmember;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.taskmanager.config.DBContainerExtension;
import com.manager.taskmanager.config.TestDataFactory;
import com.manager.taskmanager.department.entity.Department;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.member.entity.Role;
import com.manager.taskmanager.project.entity.Project;
import com.manager.taskmanager.projectmember.dto.ProjectMemberRegisterDto;
import com.manager.taskmanager.projectmember.dto.ProjectMemberUpdateDto;
import com.manager.taskmanager.projectmember.entity.ProjectMember;
import com.manager.taskmanager.projectmember.entity.ProjectMemberStatus;
import com.manager.taskmanager.projectmember.entity.ProjectRole;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@ExtendWith(DBContainerExtension.class)
@AutoConfigureMockMvc
public class ProjectMemberIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    private Department department;
    private Member leader;
    private Member member;
    private Member manager;
    private Project project;

    @BeforeEach
    void setUp() {
        department = testDataFactory.createDepartment();
        leader = testDataFactory.createLeader(department);
        member = testDataFactory.createMember(department);
        manager = testDataFactory.createManager(department);
        project = testDataFactory.createProject(leader, member);
    }

    @AfterEach
    void clearAuthentication() {
        testDataFactory.clearAuthentication();
    }

    @Nested
    @DisplayName("프로젝트 멤버 추가 테스트")
    class 프로젝트_멤버_추가_테스트 {

        @Test
        @DisplayName("프로젝트 멤버 추가 정상")
        void whenValidInput_thenProjectMemberIsAdded() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            ProjectMemberRegisterDto dto = new ProjectMemberRegisterDto(
                    List.of(
                            new ProjectMemberRegisterDto.ProjectMemberDto(
                                    manager.getId(), ProjectRole.MANAGER.name(),
                                    LocalDate.now().plusDays(5), LocalDate.now().plusWeeks(3)
                            )
                    )
            );

            mockMvc.perform(post("/api/projects/{projectId}/member", project.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andDo(print());

            ProjectMember pm = findByMemberId(manager.getId());

            assertThat(pm.getProjectRole()).isEqualTo(ProjectRole.MANAGER);
            assertThat(pm.getStartDate()).isEqualTo(LocalDate.now().plusDays(5));
            assertThat(pm.getEndDate()).isEqualTo(LocalDate.now().plusWeeks(3));
            assertThat(pm.getProject().getId()).isEqualTo(project.getId());
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenInValidInput_thenReturnBadRequest() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            ProjectMemberRegisterDto dto = new ProjectMemberRegisterDto(
                    List.of(
                            new ProjectMemberRegisterDto.ProjectMemberDto(
                                    manager.getId(), null,
                                    LocalDate.now().plusDays(5), LocalDate.now().plusWeeks(3)
                            )
                    )
            );

            mockMvc.perform(post("/api/projects/{projectId}/member", project.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("비 로그인 시 401 반환")
        void whenUnAuthenticated_thenReturnUnauthorized() throws Exception {
            ProjectMemberRegisterDto dto = new ProjectMemberRegisterDto(
                    List.of(
                            new ProjectMemberRegisterDto.ProjectMemberDto(
                                    manager.getId(), null,
                                    LocalDate.now().plusDays(5), LocalDate.now().plusWeeks(3)
                            )
                    )
            );

            mockMvc.perform(post("/api/projects/{projectId}/member", project.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("프로젝트 권한 낮을 시 403 반환")
        void whenLowProjectRole_thenReturnForbidden() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            ProjectMemberRegisterDto dto = new ProjectMemberRegisterDto(
                    List.of(
                            new ProjectMemberRegisterDto.ProjectMemberDto(
                                    manager.getId(), ProjectRole.MANAGER.name(),
                                    LocalDate.now().plusDays(5), LocalDate.now().plusWeeks(3)
                            )
                    )
            );

            mockMvc.perform(post("/api/projects/{projectId}/member", project.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("회원이 없을 시 404 반환")
        void whenNotFoundMember_thenReturnNotFound() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            ProjectMemberRegisterDto dto = new ProjectMemberRegisterDto(
                    List.of(
                            new ProjectMemberRegisterDto.ProjectMemberDto(
                                    4321432L, ProjectRole.MANAGER.name(),
                                    LocalDate.now().plusDays(5), LocalDate.now().plusWeeks(3)
                            )
                    )
            );

            mockMvc.perform(post("/api/projects/{projectId}/member", project.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("프로젝트 멤버 정보 수정 테스트")
    class 프로젝트_멤버_정보_수정_테스트 {

        @Test
        @DisplayName("프로젝트 멤버 정보 수정 정상")
        void whenValidInput_thenProjectMemberIsUpdated() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            ProjectMemberUpdateDto dto = new ProjectMemberUpdateDto(
                    ProjectRole.MANAGER.name(), LocalDate.now().plusDays(10), LocalDate.now().plusWeeks(5)
            );

            mockMvc.perform(patch("/api/projects/{projectId}/member/{memberId}", project.getId(), member.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andDo(print());

            ProjectMember pm = findByMemberId(member.getId());

            assertThat(pm.getProjectRole()).isEqualTo(ProjectRole.MANAGER);
            assertThat(pm.getStartDate()).isEqualTo(LocalDate.now().plusDays(10));
            assertThat(pm.getEndDate()).isEqualTo(LocalDate.now().plusWeeks(5));
            assertThat(pm.getProject().getId()).isEqualTo(project.getId());
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenInValidInput_thenReturnBadRequest() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            ProjectMemberUpdateDto dto = new ProjectMemberUpdateDto(
                    ProjectRole.MEMBER.name(), LocalDate.now().plusDays(10), null
            );

            mockMvc.perform(patch("/api/projects/{projectId}/member/{memberId}", project.getId(), member.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("비 로그인 시 401 반환")
        void whenUnAuthenticated_thenReturnUnauthorized() throws Exception {
            ProjectMemberUpdateDto dto = new ProjectMemberUpdateDto(
                    ProjectRole.MEMBER.name(), LocalDate.now().plusDays(10), LocalDate.now().plusWeeks(5)
            );

            mockMvc.perform(patch("/api/projects/{projectId}/member/{memberId}", project.getId(), member.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("프로젝트 권한 낮을 시 403 반환")
        void whenLowProjectRole_thenReturnForbidden() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            ProjectMemberUpdateDto dto = new ProjectMemberUpdateDto(
                    ProjectRole.MEMBER.name(), LocalDate.now().plusDays(10), LocalDate.now().plusWeeks(5)
            );

            mockMvc.perform(patch("/api/projects/{projectId}/member/{memberId}", project.getId(), member.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("회원이 없을 시 404 반환")
        void whenNotFoundMember_thenReturnNotFound() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            ProjectMemberUpdateDto dto = new ProjectMemberUpdateDto(
                    ProjectRole.MEMBER.name(), LocalDate.now().plusDays(10), LocalDate.now().plusWeeks(5)
            );

            mockMvc.perform(patch("/api/projects/{projectId}/member/{memberId}", project.getId(), 432143214L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("프로젝트 멤버 삭제 테스트")
    class 프로젝트_멤버_삭제_테스트 {

        @Test
        @DisplayName("프로젝트 멤버 삭제 정상")
        void whenValidInput_thenProjectMemberIsDeleted() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            mockMvc.perform(delete("/api/projects/{projectId}/member/{memberId}", project.getId(), member.getId()))
                    .andExpect(status().isOk())
                    .andDo(print());

            ProjectMember pm = findByMemberId(member.getId());
            assertThat(pm.getEndDate()).isEqualTo(LocalDate.now());
            assertThat(pm.getProjectMemberStatus()).isEqualTo(ProjectMemberStatus.INACTIVE);

        }

        @Test
        @DisplayName("잘못된 PATH 입력 시 400 반환")
        void whenInValidPath_thenReturnBadRequest() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            mockMvc.perform(delete("/api/projects/{projectId}/member/{memberId}", project.getId(), "memberId"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("비 로그인 시 401 반환")
        void whenUnAuthenticated_thenReturnUnauthorized() throws Exception {
            mockMvc.perform(delete("/api/projects/{projectId}/member/{memberId}", project.getId(), member.getId()))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("프로젝트 권한 낮을 시 403 반환")
        void whenLowProjectRole_thenReturnForbidden() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            mockMvc.perform(delete("/api/projects/{projectId}/member/{memberId}", project.getId(), member.getId()))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("회원이 없을 시 404 반환")
        void whenNotFoundMember_thenReturnNotFound() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            mockMvc.perform(delete("/api/projects/{projectId}/member/{memberId}", project.getId(), 432143214L))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    private ProjectMember findByMemberId(Long memberId) {
        return projectMemberRepository.findAll().stream()
                .filter(m -> m.getMember().getId().equals(memberId))
                .findFirst()
                .orElseThrow();
    }
}
