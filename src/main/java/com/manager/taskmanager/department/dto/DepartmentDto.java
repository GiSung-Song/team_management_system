package com.manager.taskmanager.department.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "부서 Response DTO")
public class DepartmentDto {

    @Schema(name = "부서 ID")
    private Long id;

    @Schema(name = "부서명")
    private String departmentName;
}