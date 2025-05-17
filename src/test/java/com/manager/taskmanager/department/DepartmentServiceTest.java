package com.manager.taskmanager.department;

import com.manager.taskmanager.common.CustomException;
import com.manager.taskmanager.config.DBContainerConfig;
import com.manager.taskmanager.department.dto.AllDepartmentListDto;
import com.manager.taskmanager.department.dto.DepartmentRegisterDto;
import com.manager.taskmanager.department.entity.Department;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@Import(DBContainerConfig.class)
class DepartmentServiceTest {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    @DisplayName("부서 추가")
    void 부서_추가_테스트() {
        DepartmentRegisterDto dto = new DepartmentRegisterDto("HR");
        departmentService.registerDepartment(dto);

        Department savedDepartment = departmentRepository.findAll().get(0);

        assertThat(savedDepartment).isNotNull();
        assertThat(savedDepartment.getDepartmentName()).isEqualTo(dto.getDepartmentName());
    }

    @Test
    @DisplayName("부서 추가 시 중복")
    void 부서_추가_실패_테스트() {
        Department department = Department.builder()
                .departmentName("HR")
                .build();

        departmentRepository.save(department);

        DepartmentRegisterDto dto = new DepartmentRegisterDto("HR");

        assertThatThrownBy(() -> departmentService.registerDepartment(dto)).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("부서 목록 조회")
    void 부서_목록_조회_테스트() {
        Department hr = Department.builder()
                .departmentName("HR")
                .build();

        Department dv = Department.builder()
                .departmentName("DV")
                .build();

        Department ca = Department.builder()
                .departmentName("CA")
                .build();

        List<Department> list = List.of(hr, dv, ca);

        departmentRepository.saveAll(list);

        AllDepartmentListDto allDepartment = departmentService.getAllDepartment();

        assertThat(allDepartment.getAllDepartments())
                .hasSize(3)
                .extracting("departmentName")
                .containsExactlyInAnyOrder("HR", "DV", "CA");
    }

    @Test
    @DisplayName("부서 삭제")
    void 부서_삭제_테스트() {
        Department department = Department.builder()
                .departmentName("HR")
                .build();

        departmentRepository.save(department);

        departmentService.deleteDepartment(department.getId());

        List<Department> departments = departmentRepository.findAll();

        assertThat(departments.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("부서 삭제 시 해당 ID 없음")
    void 부서_삭제_실패_테스트() {
        Department department = Department.builder()
                .departmentName("HR")
                .build();

        departmentRepository.save(department);

        assertThatThrownBy(() -> departmentService.deleteDepartment(123214L)).isInstanceOf(CustomException.class);
    }
}