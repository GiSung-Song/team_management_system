package com.manager.taskmanager.department;

import com.manager.taskmanager.common.CustomException;
import com.manager.taskmanager.common.ErrorCode;
import com.manager.taskmanager.department.dto.AllDepartmentListDto;
import com.manager.taskmanager.department.dto.DepartmentDto;
import com.manager.taskmanager.department.dto.DepartmentRegisterDto;
import com.manager.taskmanager.department.entity.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    @Transactional
    @CacheEvict(value = "departmentList", key = "'departmentList'")
    public void registerDepartment(DepartmentRegisterDto dto) {
        if (departmentRepository.findByDepartmentName(dto.getDepartmentName()).isPresent()) {
            throw new CustomException(ErrorCode.DEPARTMENT_DUPLICATE);
        }

        Department department = Department.builder()
                .departmentName(dto.getDepartmentName())
                .build();

        departmentRepository.save(department);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "departmentList", key = "'departmentList'")
    public AllDepartmentListDto getAllDepartment() {
        return new AllDepartmentListDto(departmentRepository.findAll().stream()
                .map(department -> new DepartmentDto(department.getId(), department.getDepartmentName()))
                .toList());
    }

    @Transactional
    @CacheEvict(value = "departmentList", key = "'departmentList'")
    public void deleteDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND));

        departmentRepository.delete(department);
    }
}