package com.manager.taskmanager.projectmember;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.taskmanager.global.config.security.JwtTokenFilter;
import com.manager.taskmanager.projectmember.dto.ProjectMemberRegisterDto;
import com.manager.taskmanager.projectmember.dto.ProjectMemberUpdateDto;
import com.manager.taskmanager.projectmember.entity.ProjectRole;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = ProjectMemberController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class))
class ProjectMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectMemberService projectMemberService;

    @Nested
    @DisplayName("프로젝트 멤버 추가 Controller")
    class 프로젝트_멤버_추가_컨트롤러_테스트 {

        @Test
        @DisplayName("프로젝트 멤버 추가 정상")
        void whenValidInput_thenMemberIsAdded() throws Exception {
            doNothing().when(projectMemberService).addProjectMember(anyLong(), anyLong(), any(ProjectMemberRegisterDto.class));

            ProjectMemberRegisterDto dto = new ProjectMemberRegisterDto(
                    List.of(
                            new ProjectMemberRegisterDto.ProjectMemberDto(
                                    1L, ProjectRole.MEMBER.name(),
                                    LocalDate.now().plusDays(5), LocalDate.now().plusWeeks(3)
                            )
                    )
            );

            mockMvc.perform(post("/api/projects/{projectId}/member", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andDo(print());
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenInValidInput_thenReturnBadRequest() throws Exception {
            ProjectMemberRegisterDto dto = new ProjectMemberRegisterDto(
                    List.of(
                            new ProjectMemberRegisterDto.ProjectMemberDto(
                                    1L, null,
                                    LocalDate.now().plusDays(5), LocalDate.now().plusWeeks(3)
                            )
                    )
            );

            mockMvc.perform(post("/api/projects/{projectId}/member", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("프로젝트 멤버 정보 수정 Controller")
    class 프로젝트_멤버_정보_수정_컨트롤러_테스트 {

        @Test
        @DisplayName("프로젝트 멤버 정보 수정 정상")
        void whenValidInput_thenProjectMemberIsUpdated() throws Exception {
            doNothing().when(projectMemberService).updateProjectMember(anyLong(), anyLong(), anyLong(), any(ProjectMemberUpdateDto.class));

            ProjectMemberUpdateDto projectMemberUpdateDto =
                    new ProjectMemberUpdateDto(ProjectRole.MANAGER.name(), LocalDate.now(), LocalDate.now().plusMonths(5));

            mockMvc.perform(patch("/api/projects/{projectId}/member/{memberId}", 1L, 5L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(projectMemberUpdateDto)))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenInValidInput_thenReturnBadRequest() throws Exception {
            ProjectMemberUpdateDto projectMemberUpdateDto =
                    new ProjectMemberUpdateDto(null, LocalDate.now(), LocalDate.now().plusMonths(5));

            mockMvc.perform(patch("/api/projects/{projectId}/member/{memberId}", 1L, 5L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(projectMemberUpdateDto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("프로젝트 멤버 삭제 Controller")
    class 프로젝트_멤버_삭제_컨트롤러_테스트 {

        @Test
        @DisplayName("프로젝트 멤버 삭제 정상")
        void whenValidInput_thenProjectMemberIsUpdated() throws Exception {
            doNothing().when(projectMemberService).deleteProjectMember(anyLong(), anyLong(), anyLong());

            mockMvc.perform(delete("/api/projects/{projectId}/member/{memberId}", 1L, 5L))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        @DisplayName("잘못된 path 입력 시 400 반환")
        void whenInValidPath_thenReturnBadRequest() throws Exception {
            mockMvc.perform(delete("/api/projects/{projectId}/member/{memberId}", "projectId", "memberId"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }
}