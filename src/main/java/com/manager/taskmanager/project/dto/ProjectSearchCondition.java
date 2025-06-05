package com.manager.taskmanager.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "프로젝트 검색 Parameter DTO")
public class ProjectSearchCondition {

    @Schema(description = "프로젝트명", example = "PROJECT-ABRACADABRA")
    private String projectName;

    @Schema(description = "이름", example = "TESTER")
    private String memberName;

    @Schema(description = "프로젝트 상태", example = "PENDING")
    private String projectStatus;
}
