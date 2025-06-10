package com.manager.taskmanager.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.taskmanager.global.config.security.JwtTokenFilter;
import com.manager.taskmanager.task.dto.AddTaskDto;
import com.manager.taskmanager.task.dto.UpdateTaskDto;
import com.manager.taskmanager.task.entity.TaskStatus;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = TaskController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class))
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Nested
    @DisplayName("업무 등록 Controller")
    class 업무_등록_컨트롤러_테스트 {

        @Test
        @DisplayName("업무 등록 정상")
        void whenValidInput_thenTaskIsAdded() throws Exception {
            AddTaskDto addTaskDto = new AddTaskDto(
                    "task", "description", LocalDate.now(),
                    LocalDate.now().plusDays(1), TaskStatus.PROGRESS.name()
            );

            willDoNothing().given(taskService).addTask(anyLong(), anyLong(), any(AddTaskDto.class));

            mockMvc.perform(post("/api/projects/{projectsId}/tasks", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addTaskDto)))
                    .andDo(print())
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenMissingRequiredField_thenReturnBadRequest() throws Exception {
            AddTaskDto addTaskDto = new AddTaskDto(
                    null, "description", LocalDate.now(),
                    LocalDate.now().plusDays(1), TaskStatus.PROGRESS.name()
            );

            mockMvc.perform(post("/api/projects/{projectsId}/tasks", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addTaskDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("업무 수정 Controller")
    class 업무_수정_컨트롤러_테스트 {

        @Test
        @DisplayName("업무 수정 정상")
        void whenValidInput_thenTaskIsAdded() throws Exception {
            UpdateTaskDto updateTaskDto =
                    new UpdateTaskDto("update-description", LocalDate.now(), LocalDate.now().plusDays(5), TaskStatus.COMPLETED.name());

            willDoNothing().given(taskService).updateTask(anyLong(), anyLong(), any(UpdateTaskDto.class));

            mockMvc.perform(patch("/api/tasks/{taskId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateTaskDto)))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenMissingRequiredField_thenReturnBadRequest() throws Exception {
            UpdateTaskDto updateTaskDto =
                    new UpdateTaskDto(null, LocalDate.now(), LocalDate.now().plusDays(5), TaskStatus.COMPLETED.name());

            mockMvc.perform(patch("/api/tasks/{taskId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateTaskDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("업무 삭제 Controller")
    class 업무_삭제_컨트롤러_테스트 {

        @Test
        @DisplayName("업무 삭제 정상")
        void whenValidInput_thenTaskIsAdded() throws Exception {
            willDoNothing().given(taskService).deleteTask(anyLong(), anyLong());

            mockMvc.perform(delete("/api/tasks/{taskId}", 1L))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("path 변환 실패 시 400 반환 ")
        void whenInvalidPath_thenReturnBadRequest() throws Exception {
            mockMvc.perform(delete("/api/tasks/{taskId}", "taskId"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}