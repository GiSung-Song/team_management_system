package com.manager.taskmanager.task.dto;

import com.manager.taskmanager.common.ValidEnum;
import com.manager.taskmanager.task.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업무 수정 Request DTO")
public class UpdateTaskDto {

    @Schema(description = "설명", example = "This is Making Service")
    @NotBlank(message = "설명은 필수 입력 값 입니다.")
    private String description;

    @Schema(description = "시작일", example = "2025-01-01")
    @NotNull(message = "시작일은 필수 입력 값 입니다.")
    private LocalDate startDate;

    @Schema(description = "종료일", example = "2025-12-31")
    @NotNull(message = "종료일은 필수 입력 값 입니다.")
    private LocalDate endDate;

    @Schema(description = "진행 상태", example = "PENDING")
    @NotNull(message = "진행 상태는 필수 입력 값 입니다.")
    @ValidEnum(enumClass = TaskStatus.class)
    private String taskStatus;

}
