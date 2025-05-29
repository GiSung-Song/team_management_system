package com.manager.taskmanager.project.dto;

import com.manager.taskmanager.member.entity.Position;
import com.manager.taskmanager.projectmember.entity.ProjectMemberStatus;
import com.manager.taskmanager.projectmember.entity.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "프로젝트 상세 조회 Response DTO")
public class ProjectDetailDto {

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

    @Schema(description = "프로젝트 회원 목록")
    private List<MemberInfo> projectMembers;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "프로젝트 회원 정보 Response DTO")
    public static class MemberInfo {

        @Schema(description = "회원 ID")
        private Long memberId;

        @Schema(description = "이름")
        private String name;

        @Schema(description = "휴대폰 번호")
        private String phoneNumber;

        @Schema(description = "프로젝트 합류 시작 날짜")
        private LocalDate startDate;

        @Schema(description = "프로젝트 합류 종료 날짜")
        private LocalDate endDate;

        @Schema(description = "프로젝트 합류 상태")
        private String projectMemberStatus;

        @Schema(description = "프로젝트 역할")
        private String projectRole;

        @Schema(description = "직급")
        private String position;

        @Schema(description = "부서")
        private String departmentName;

        public MemberInfo(Long memberId, String name, String phoneNumber, LocalDate startDate, LocalDate endDate,
                          ProjectMemberStatus status, ProjectRole projectRole, Position position, String departmentName) {
            this.memberId = memberId;
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.startDate = startDate;
            this.endDate = endDate;
            this.projectMemberStatus = status.name();
            this.projectRole = projectRole.getKorean();
            this.position = position.getKorean();
            this.departmentName = departmentName;
        }
    }
}
