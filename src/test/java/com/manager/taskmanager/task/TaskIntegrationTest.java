package com.manager.taskmanager.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.taskmanager.config.DBContainerExtension;
import com.manager.taskmanager.config.TestDataFactory;
import com.manager.taskmanager.department.entity.Department;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.member.entity.Role;
import com.manager.taskmanager.project.entity.Project;
import com.manager.taskmanager.task.dto.AddTaskDto;
import com.manager.taskmanager.task.dto.UpdateTaskDto;
import com.manager.taskmanager.task.entity.Task;
import com.manager.taskmanager.task.entity.TaskStatus;
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
public class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    private Department department;
    private Member leader;
    private Member member;
    private Member manager;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        department = testDataFactory.createDepartment();
        leader = testDataFactory.createLeader(department);
        member = testDataFactory.createMember(department);
        manager = testDataFactory.createManager(department);
        project = testDataFactory.createProject(leader, member);
        task = testDataFactory.createTask(member, project);
    }

    @AfterEach
    void clearAuthentication() {
        testDataFactory.clearAuthentication();
    }

    @Nested
    @DisplayName("업무 등록 테스트")
    class 업무_등록_테스트 {

        @Test
        @DisplayName("업무 등록 정상")
        void whenValidInput_thenTaskIsAdded() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            AddTaskDto addTaskDto = new AddTaskDto("test-task", "test-description",
                    LocalDate.now(), LocalDate.now().plusDays(5), TaskStatus.PROGRESS.name());

            mockMvc.perform(post("/api/projects/{projectId}/tasks", project.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addTaskDto)))
                    .andDo(print())
                    .andExpect(status().isCreated());

            Task findTask = findByTaskName(addTaskDto.getTaskName());

            assertThat(findTask.getTaskStatus()).isEqualTo(TaskStatus.PROGRESS);
            assertThat(findTask.getDescription()).isEqualTo(addTaskDto.getDescription());
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenMissingRequiredField_thenReturnBadRequest() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            AddTaskDto addTaskDto = new AddTaskDto(null, "test-description",
                    LocalDate.now(), LocalDate.now().plusDays(5), TaskStatus.PROGRESS.name());

            mockMvc.perform(post("/api/projects/{projectId}/tasks", project.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addTaskDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("미 인증 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            AddTaskDto addTaskDto = new AddTaskDto(null, "test-description",
                    LocalDate.now(), LocalDate.now().plusDays(5), TaskStatus.PROGRESS.name());

            mockMvc.perform(post("/api/projects/{projectId}/tasks", project.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addTaskDto)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("프로젝트 멤버가 아닐 시 404 반환")
        void whenMemberNotInProject_thenReturnNotFound() throws Exception {
            testDataFactory.setAuthentication(manager, Role.MEMBER);

            AddTaskDto addTaskDto = new AddTaskDto("test-task", "test-description",
                    LocalDate.now(), LocalDate.now().plusDays(5), TaskStatus.PROGRESS.name());

            mockMvc.perform(post("/api/projects/{projectId}/tasks", project.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addTaskDto)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("업무 수정 테스트")
    class 업무_수정_테스트 {

        @Test
        @DisplayName("업무 수정 정상")
        void whenValidInput_thenTaskIsUpdated() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            UpdateTaskDto updateTaskDto =
                    new UpdateTaskDto("update-description", task.getStartDate(), task.getEndDate(), task.getTaskStatus().name());

            mockMvc.perform(patch("/api/tasks/{taskId}", task.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateTaskDto)))
                    .andDo(print())
                    .andExpect(status().isOk());

            Task findTask = findByTaskName(task.getTaskName());

            assertThat(findTask.getDescription()).isEqualTo(updateTaskDto.getDescription());
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenMissingRequiredField_thenReturnBadRequest() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            UpdateTaskDto updateTaskDto =
                    new UpdateTaskDto(null, task.getStartDate(), task.getEndDate(), task.getTaskStatus().name());

            mockMvc.perform(patch("/api/tasks/{taskId}", task.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateTaskDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("미 인증 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            UpdateTaskDto updateTaskDto =
                    new UpdateTaskDto("update-description", task.getStartDate(), task.getEndDate(), task.getTaskStatus().name());

            mockMvc.perform(patch("/api/tasks/{taskId}", task.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateTaskDto)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("자신의 업무가 아닐 시 403 반환")
        void whenNotTaskOwner_thenReturnForbidden() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            UpdateTaskDto updateTaskDto =
                    new UpdateTaskDto("update-description", task.getStartDate(), task.getEndDate(), task.getTaskStatus().name());

            mockMvc.perform(patch("/api/tasks/{taskId}", task.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateTaskDto)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("프로젝트 멤버가 아닐 시 404 반환")
        void whenMemberNotInProject_thenReturnNotFound() throws Exception {
            testDataFactory.setAuthentication(manager, Role.MEMBER);

            UpdateTaskDto updateTaskDto =
                    new UpdateTaskDto("update-description", task.getStartDate(), task.getEndDate(), task.getTaskStatus().name());

            mockMvc.perform(patch("/api/tasks/{taskId}", task.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateTaskDto)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("업무 목록 조회 테스트")
    class 업무_목록_조회_테스트 {

        @Test
        @DisplayName("업무 목록 조회 정상 - Role.MEMBER")
        void whenParamProjectNameForMember_thenReturnTaskList() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            mockMvc.perform(get("/api/tasks")
                            .param("projectName", project.getProjectName()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.taskList[*].taskName", containsInAnyOrder(task.getTaskName())));
        }

        @Test
        @DisplayName("업무 목록 조회 정상 - Role.MANAGER")
        void whenParamProjectNameForManager_thenReturnTaskList() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MANAGER);

            mockMvc.perform(get("/api/tasks")
                            .param("projectName", project.getProjectName()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.taskList[*].taskName", containsInAnyOrder(task.getTaskName())));
        }

        @Test
        @DisplayName("미 인증 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/tasks")
                            .param("projectName", project.getProjectName()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("업무 상세 조회 테스트")
    class 업무_상세_조회_테스트 {

        @Test
        @DisplayName("업무 상세 조회 정상 - Role.MEMBER")
        void whenForMember_thenReturnTaskList() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            mockMvc.perform(get("/api/tasks/{taskId}", task.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.taskName").value(task.getTaskName()));
        }

        @Test
        @DisplayName("업무 상세 조회 정상 - Role.MANAGER")
        void whenForManager_thenReturnTaskList() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MANAGER);

            mockMvc.perform(get("/api/tasks/{taskId}", task.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.taskName").value(task.getTaskName()));
        }

        @Test
        @DisplayName("잘못된 path 입력 시 400 반환")
        void whenInvalidPath_thenReturnBadRequest() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            mockMvc.perform(get("/api/tasks/{taskId}", "taskId"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("미 인증 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/tasks/{taskId}", task.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("자신의 업무가 아닐 시 403 반환")
        void whenNotTaskOwner_thenReturnForbidden() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            mockMvc.perform(get("/api/tasks/{taskId}", task.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("프로젝트 멤버가 아닐 시 404 반환")
        void whenMemberNotInProject_thenReturnNotFound() throws Exception {
            testDataFactory.setAuthentication(manager, Role.MEMBER);

            mockMvc.perform(get("/api/tasks/{taskId}", task.getId()))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("업무 삭제 테스트")
    class 업무_삭제_테스트 {

        @Test
        @DisplayName("업무 삭제 정상")
        void whenForMember_thenReturnTaskList() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            mockMvc.perform(delete("/api/tasks/{taskId}", task.getId()))
                    .andDo(print())
                    .andExpect(status().isOk());

            Task findTask = findByTaskName(task.getTaskName());

            assertThat(findTask.getDeletedAt()).isNotNull();
            assertThat(findTask.getTaskStatus()).isEqualTo(TaskStatus.CANCELED);
        }

        @Test
        @DisplayName("잘못된 path 입력 시 400 반환")
        void whenInvalidPath_thenReturnBadRequest() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            mockMvc.perform(delete("/api/tasks/{taskId}", "taskId"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("미 인증 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            mockMvc.perform(delete("/api/tasks/{taskId}", task.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("자신의 업무가 아닐 시 403 반환")
        void whenNotTaskOwner_thenReturnForbidden() throws Exception {
            testDataFactory.setAuthentication(leader, Role.MEMBER);

            mockMvc.perform(delete("/api/tasks/{taskId}", task.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("프로젝트 멤버가 아닐 시 404 반환")
        void whenMemberNotInProject_thenReturnNotFound() throws Exception {
            testDataFactory.setAuthentication(manager, Role.MEMBER);

            mockMvc.perform(delete("/api/tasks/{taskId}", task.getId()))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    private Task findByTaskName(String taskName) {
        return taskRepository.findAll().stream()
                .filter(task -> task.getTaskName().equalsIgnoreCase(taskName))
                .findFirst()
                .orElseThrow();
    }
}
