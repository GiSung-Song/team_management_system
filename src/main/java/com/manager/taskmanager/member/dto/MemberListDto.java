package com.manager.taskmanager.member.dto;

import com.manager.taskmanager.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 목록 정보 Response DTO")
public class MemberListDto {

    @Schema(name = "회원 ID")
    private Long id;

    @Schema(name = "이름")
    private String name;

    @Schema(name = "직급")
    private String position;

    @Schema(name = "부서명")
    private String departmentName;

    public static MemberListDto from(Member member) {
        return new MemberListDto(
                member.getId(),
                member.getName(),
                member.getPosition().getKorean(),
                member.getDepartment().getDepartmentName()
        );
    }
}
