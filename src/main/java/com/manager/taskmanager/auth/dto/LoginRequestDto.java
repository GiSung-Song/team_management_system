package com.manager.taskmanager.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 Request DTO")
public class LoginRequestDto {

    @Schema(description = "사번", example = "EMP0001", maxLength = 50)
    @NotBlank(message = "사번은 필수 입력 값 입니다.")
    @Length(max = 50, message = "사번은 최대 50자 입니다.")
    private String employeeNumber;

    @Schema(description = "비밀번호")
    @NotBlank(message = "비밀번호는 필수 입력 값 입니다.")
    private String password;
}
