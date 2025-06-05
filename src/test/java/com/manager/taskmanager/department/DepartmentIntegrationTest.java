package com.manager.taskmanager.department;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.taskmanager.config.DBContainerExtension;
import com.manager.taskmanager.department.dto.DepartmentRegisterDto;
import com.manager.taskmanager.department.entity.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@ExtendWith(DBContainerExtension.class)
@AutoConfigureMockMvc
public class DepartmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    Department hr;
    Department cb;

    @BeforeEach
    void setUpDepartment() {
        hr = Department.builder()
                .departmentName("HR")
                .build();

        cb = Department.builder()
                .departmentName("CB")
                .build();

        List<Department> departments = List.of(
                hr, cb
        );

        departmentRepository.saveAll(departments);
        departmentRepository.flush();
    }

    @Test
    @DisplayName("부서 목록 조회 API")
    @WithMockUser(roles = "MEMBER")
    void whenRequestIsValid_thenReturnAllDepartmentList() throws Exception {
        mockMvc.perform(get("/api/departments")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.allDepartments[0].departmentName").value("HR"))
                .andExpect(jsonPath("$.data.allDepartments[1].departmentName").value("CB"));
    }

    @Nested
    @DisplayName("부서 등록 API")
    class 부서_등록_테스트 {

        @Test
        @DisplayName("정상 등록 요청 시 201 반환")
        @WithMockUser(roles = "MANAGER")
        void whenValidInput_thenCreateDepartmentIsCreated() throws Exception {
            DepartmentRegisterDto dto = new DepartmentRegisterDto("CA");

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andDo(print());
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        @WithMockUser(roles = "MANAGER")
        void whenRequiredFieldMissing_thenReturnBadRequest() throws Exception {
            DepartmentRegisterDto dto = new DepartmentRegisterDto();

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("중복 값 요청 시 409 반환")
        @WithMockUser(roles = "MANAGER")
        void whenDepartmentNameIsDuplicated_thenReturnBadRequest() throws Exception {
            DepartmentRegisterDto dto = new DepartmentRegisterDto("HR");

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict())
                    .andDo(print());
        }

        @Test
        @DisplayName("인증 실패 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            DepartmentRegisterDto dto = new DepartmentRegisterDto("QA");

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("권한 없을 시 403 반환")
        @WithMockUser(roles = "MEMBER")
        void whenUnauthorized_thenReturnForbidden() throws Exception {
            DepartmentRegisterDto dto = new DepartmentRegisterDto("QA");

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("부서 삭제 API")
    class 부서_삭제_테스트 {

        @Test
        @DisplayName("정상 삭제 요청 시 200 반환")
        @WithMockUser(roles = "MANAGER")
        void whenValidInput_thenDepartmentDeleted() throws Exception {
            mockMvc.perform(delete("/api/departments/{departmentId}", hr.getId()))
                    .andDo(print())
                    .andExpect(status().isOk());

            List<Department> allDepartment = departmentRepository.findAll();

            assertThat(allDepartment.size()).isEqualTo(1);
            assertThat(allDepartment.get(0).getDepartmentName()).isEqualTo("CB");
        }

        @Test
        @DisplayName("잘못된 요청 시 400 반환")
        @WithMockUser(roles = "MANAGER")
        void whenInvalid_thenReturnBadRequest() throws Exception {
            mockMvc.perform(delete("/api/departments/{departmentId}", "departmentId"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("인증 실패 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            mockMvc.perform(delete("/api/departments/{departmentId}", hr.getId()))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("권한 없을 시 403 반환")
        @WithMockUser(roles = "MEMBER")
        void whenUnauthorized_thenReturnForbidden() throws Exception {
            mockMvc.perform(delete("/api/departments/{departmentId}", hr.getId()))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }
    }


}