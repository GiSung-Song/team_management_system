package com.manager.taskmanager.department;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.taskmanager.global.config.security.JwtTokenFilter;
import com.manager.taskmanager.department.dto.AllDepartmentListDto;
import com.manager.taskmanager.department.dto.DepartmentDto;
import com.manager.taskmanager.department.dto.DepartmentRegisterDto;
import org.junit.jupiter.api.DisplayName;
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

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = DepartmentController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class))
public class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @Test
    @DisplayName("부서 전체 목록 조회")
    void 부서_조회_테스트() throws Exception {
        AllDepartmentListDto allDepartment = new AllDepartmentListDto(List.of(
                new DepartmentDto(0L, "AA"),
                new DepartmentDto(1L, "BB"),
                new DepartmentDto(2L, "CC"),
                new DepartmentDto(3L, "DD")
        ));

        given(departmentService.getAllDepartment()).willReturn(allDepartment);

        mockMvc.perform(get("/api/departments")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.allDepartments[0].departmentName").value("AA"))
                .andExpect(jsonPath("$.data.allDepartments[1].departmentName").value("BB"))
                .andExpect(jsonPath("$.data.allDepartments[2].departmentName").value("CC"))
                .andExpect(jsonPath("$.data.allDepartments[3].departmentName").value("DD"));
    }

    @Test
    @DisplayName("부서 등록")
    void 부서_등록_테스트() throws Exception {
        DepartmentRegisterDto dto = new DepartmentRegisterDto("CC");

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("부서 등록 시 필수 값 누락")
    void 부서_등록_실패_테스트() throws Exception {
        DepartmentRegisterDto dto = new DepartmentRegisterDto();

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("부서 삭제 테스트")
    void 부서_삭제_테스트() throws Exception {
        mockMvc.perform(delete("/api/departments/{id}", 1L))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
