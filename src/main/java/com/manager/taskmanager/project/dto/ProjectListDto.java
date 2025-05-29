package com.manager.taskmanager.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "프로젝트 목록 Response DTO")
public class ProjectListDto {

    @Schema(description = "프로젝트 목록")
    List<ProjectInfo> projectList = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "프로젝트 목록 정보 Response DTO")
    public static class ProjectInfo {

        @Schema(description = "프로젝트명")
        private String projectName;

        @Schema(description = "시작일")
        private LocalDate startDate;

        @Schema(description = "종료일")
        private LocalDate endDate;

        @Schema(description = "프로젝트 상태")
        private String projectStatus;

        @Schema(description = "삭제일자")
        private LocalDateTime deletedAt;
    }
}
