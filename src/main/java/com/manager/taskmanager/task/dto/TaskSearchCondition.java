package com.manager.taskmanager.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업무 검색 Parameter DTO")
public class TaskSearchCondition {

    @Schema(description = "프로젝트명", example = "PROJECT-ABRACADABRA")
    private String projectName;

    @Schema(description = "업무명", example = "TASK-BETA")
    private String taskName;

    @Schema(description = "업무 상태", example = "PENDING")
    private String taskStatus;
}
