package com.manager.taskmanager.projectmember.dto;

import com.manager.taskmanager.common.ValidEnum;
import com.manager.taskmanager.projectmember.entity.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "프로젝트 멤버 추가 Request DTO")
public class ProjectMemberRegisterDto {

    @Valid
    @NotEmpty(message = "최소 한 명 이상 등록하세요.")
    List<ProjectMemberDto> projectMemberDtoList = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "프로젝트 멤버 정보 Request DTO")
    public static class ProjectMemberDto {

        @Schema(description = "회원 ID")
        @NotNull(message = "회원 ID는 필수 입력 값 입니다.")
        private Long memberId;

        @Schema(description = "직책", example = "MEMBER")
        @NotNull(message = "직책은 필수 입력 값 입니다.")
        @ValidEnum(enumClass = ProjectRole.class)
        private String projectRole;

        @Schema(description = "시작일", example = "2025-01-01")
        @NotNull(message = "시작일은 필수 입력 값 입니다.")
        private LocalDate startDate;

        @Schema(description = "종료일", example = "2025-12-31")
        @NotNull(message = "종료일은 필수 입력 값 입니다.")
        private LocalDate endDate;

    }
}
