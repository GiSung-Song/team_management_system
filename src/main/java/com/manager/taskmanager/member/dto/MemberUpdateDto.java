package com.manager.taskmanager.member.dto;

import com.manager.taskmanager.common.ValidEnum;
import com.manager.taskmanager.member.entity.Position;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 정보 수정 Request DTO")
public class MemberUpdateDto {

    @Schema(description = "이메일", example = "email@email.com", maxLength = 100)
    @Email(message = "이메일 형식으로 작성해주세요.")
    @NotBlank(message = "이메일은 필수 입력 값 입니다.")
    @Length(max = 100, message = "이메일은 최대 100자 입니다.")
    private String email;

    @Schema(description = "휴대폰 번호", example = "01012341234", maxLength = 20)
    @NotBlank(message = "휴대폰 번호는 필수 입력 값 입니다.")
    @Length(max = 20, message = "휴대폰 번호는 최대 20자 입니다.")
    private String phoneNumber;

    @Schema(description = "직급", example = "INTERN")
    @NotNull(message = "직급은 필수 입력 값 입니다.")
    @ValidEnum(enumClass = Position.class)
    private String position;

    @Schema(description = "부서 ID")
    @NotNull(message = "부서는 필수 입력 값 입니다.")
    private Long departmentId;
}
