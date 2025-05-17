package com.manager.taskmanager.department.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "부서 전체 목록 Response DTO")
public class AllDepartmentListDto {

    @Schema(name = "부서 전체 목록")
    private List<DepartmentDto> allDepartments;
}
