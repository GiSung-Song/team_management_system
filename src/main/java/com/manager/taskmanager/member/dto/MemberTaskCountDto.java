package com.manager.taskmanager.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 업무 갯수 Response DTO")
public class MemberTaskCountDto {

    private Long memberId;
    private Long taskCount;
}
