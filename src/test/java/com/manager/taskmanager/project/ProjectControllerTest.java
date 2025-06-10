package com.manager.taskmanager.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.taskmanager.global.config.security.JwtTokenFilter;
import com.manager.taskmanager.project.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = ProjectController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class))
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @Nested
    @DisplayName("프로젝트 생성 Controller")
    class 프로젝트_생성_컨트롤러_테스트 {

        @Test
        @DisplayName("프로젝트 생성 정상")
        void whenValidInput_thenProjectIsCreated() throws Exception {
            ProjectRegisterDto projectRegisterDto = new ProjectRegisterDto(
                    "team-project", "project description", LocalDate.now(),
                    LocalDate.now().plusWeeks(5), "PENDING"
            );

            willDoNothing().given(projectService).createProject(anyLong(), any(ProjectRegisterDto.class));

            mockMvc.perform(post("/api/projects")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectRegisterDto)))
                    .andExpect(status().isCreated())
                    .andDo(print());
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenMissingRequiredField_thenReturnBadRequest() throws Exception {
            ProjectRegisterDto projectRegisterDto = new ProjectRegisterDto(
                    null, "project description", LocalDate.now(),
                    LocalDate.now().plusWeeks(5), "PENDING"
            );

            willDoNothing().given(projectService).createProject(anyLong(), any(ProjectRegisterDto.class));

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(projectRegisterDto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("프로젝트 수정 Controller")
    class 프로젝트_수정_컨트롤러_테스트 {

        @Test
        @DisplayName("프로젝트 정상 수정")
        void whenValidInput_thenProjectIsUpdated() throws Exception {
            ProjectUpdateDto projectUpdateDto = new ProjectUpdateDto(
                    "new description", LocalDate.now(), LocalDate.now().plusWeeks(1), "COMPLETED"
            );

            willDoNothing().given(projectService).updateProject(anyLong(), anyLong(), any(ProjectUpdateDto.class));

            mockMvc.perform(patch("/api/projects/{projectId}", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectUpdateDto)))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenMissingRequiredField_thenReturnBadRequest() throws Exception {
            ProjectUpdateDto projectUpdateDto = new ProjectUpdateDto(
                    "new description", null, LocalDate.now().plusWeeks(1), "COMPLETED"
            );

            willDoNothing().given(projectService).updateProject(anyLong(), anyLong(), any(ProjectUpdateDto.class));

            mockMvc.perform(patch("/api/projects/{projectId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(projectUpdateDto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("프로젝트 목록 조회 Controller")
    class 프로젝트_목록_조회_컨트롤러_테스트 {

        @Test
        @DisplayName("프로젝트 목록 정상 조회")
        void whenValidInput_thenReturnProjectList() throws Exception {
            ProjectListDto projectListDto = new ProjectListDto(
                    List.of(
                            new ProjectListDto.ProjectInfo(
                                    "프로젝트 이름1", LocalDate.now(), LocalDate.now().plusWeeks(5), "PROGRESS", null),
                            new ProjectListDto.ProjectInfo(
                                    "프로젝트 이름2", LocalDate.now(), LocalDate.now().plusWeeks(5), "CANCELED", LocalDateTime.now())
                    )
            );

            given(projectService.getProjectList(any(ProjectSearchCondition.class))).willReturn(projectListDto);

            mockMvc.perform(get("/api/projects"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projectList[*].projectName", containsInAnyOrder("프로젝트 이름1", "프로젝트 이름2")));
        }
    }

    @Nested
    @DisplayName("프로젝트 상세 조회 Controller")
    class 프로젝트_상세_조회_컨트롤러_테스트 {

        @Test
        @DisplayName("프로젝트 상세 조회 정상")
        void whenValidInput_thenReturnProjectDetail() throws Exception {
            ProjectDetailDto projectDetailDto = new ProjectDetailDto(
                    "프로젝트", LocalDate.now(), LocalDate.now().plusWeeks(10), "PENDING", null,
                    List.of(
                            new ProjectDetailDto.MemberInfo(
                                    0L, "리더", "01012341234", LocalDate.now(), LocalDate.now().plusWeeks(10),
                                    "ACTIVE", "LEADER", "EXECUTIVE_VICE_PRESIDENT", "HR"
                            )
                    )
            );

            given(projectService.getProjectDetail(anyLong())).willReturn(projectDetailDto);

            mockMvc.perform(get("/api/projects/{projectId}", 1L))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.data.projectName").value("프로젝트"));
        }

        @Test
        @DisplayName("프로젝트 ID 값 오류 시 400 반환")
        void whenInvalidPath_thenReturnBadRequest() throws Exception {
            mockMvc.perform(get("/api/projects/{projectId}", "projectId"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("프로젝트 삭제 Controller")
    class 프로젝트_삭제_컨트롤러_테스트 {

        @Test
        @DisplayName("프로젝트 삭제 정상")
        void whenValidInput_thenReturnProjectDetail() throws Exception {
            willDoNothing().given(projectService).deleteProject(anyLong(), anyLong());

            mockMvc.perform(delete("/api/projects/{projectId}", 1L))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        @DisplayName("프로젝트 ID 값 오류 시 400 반환")
        void whenInvalidPath_thenReturnBadRequest() throws Exception {
            mockMvc.perform(get("/api/projects/{projectId}", "projectId"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }
}