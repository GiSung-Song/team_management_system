package com.manager.taskmanager.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 수정 Request DTO")
public class PasswordUpdateDto {

    @Schema(description = "비밀번호")
    @NotBlank(message = "비밀번호는 필수 입력 값 입니다.")
    private String password;
}