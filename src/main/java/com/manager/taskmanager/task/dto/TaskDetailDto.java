package com.manager.taskmanager.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업무 정보 Response DTO")
public class TaskDetailDto {

    @Schema(description = "프로젝트명")
    private String projectName;

    @Schema(description = "업무명")
    private String taskName;

    @Schema(description = "업무 설명")
    private String description;

    @Schema(description = "시작일")
    private LocalDate startDate;

    @Schema(description = "종료일")
    private LocalDate endDate;

    @Schema(description = "업무 상태")
    private String taskStatus;

    @Schema(description = "삭제일자")
    private LocalDateTime deletedAt;
}
