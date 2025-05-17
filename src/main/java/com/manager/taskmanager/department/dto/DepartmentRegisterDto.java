package com.manager.taskmanager.department.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "부서 추가 Request DTO")
public class DepartmentRegisterDto {

    @Schema(name = "부서명", example = "HR")
    @NotBlank(message = "부서명은 필수 입력 값 입니다.")
    @Length(max = 50, message = "부서명은 최대 50자 입니다.")
    private String departmentName;
}
