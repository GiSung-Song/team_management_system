package com.manager.taskmanager.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업무 목록 Response DTO")
public class TaskListDto {

    @Schema(description = "업무 목록")
    List<TaskListDto.TaskInfo> taskList = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "업무 목록 정보 Response DTO")
    public static class TaskInfo {

        @Schema(description = "프로젝트명")
        private String projectName;

        @Schema(description = "업무명")
        private String taskName;

        @Schema(description = "업무 상태")
        private String taskStatus;

        @Schema(description = "삭제일자")
        private LocalDateTime deletedAt;
    }
}
