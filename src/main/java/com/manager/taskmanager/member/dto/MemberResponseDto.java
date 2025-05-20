package com.manager.taskmanager.member.dto;

import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.member.entity.Position;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 정보 Response DTO")
public class MemberResponseDto {

    @Schema(name = "회원 ID")
    private Long id;

    @Schema(name = "사번")
    private String employeeNumber;

    @Schema(name = "이름")
    private String name;

    @Schema(name = "이메일")
    private String email;

    @Schema(name = "휴대폰 번호")
    private String phoneNumber;

    @Schema(name = "직급")
    private PositionDto positionDto;

    @Schema(name = "부서명")
    private String departmentName;

    @Schema(name = "삭제 시각")
    private LocalDateTime deletedAt;

    @AllArgsConstructor
    @Getter
    @Setter
    @NoArgsConstructor
    public static class PositionDto {
        private String position;
        private String positionName;
        private int level;

        public static PositionDto from(Position position) {
            return new PositionDto(position.name(), position.getKorean(), position.getLevel());
        }
    }

    public static MemberResponseDto from(Member member) {
        if (member == null) {
            return null;
        }

        return new MemberResponseDto(
                member.getId(),
                member.getEmployeeNumber(),
                member.getName(),
                member.getEmail(),
                member.getPhoneNumber(),
                PositionDto.from(member.getPosition()),
                member.getDepartment().getDepartmentName(),
                member.getDeletedAt() != null ? member.getDeletedAt() : null
        );
    }
}
