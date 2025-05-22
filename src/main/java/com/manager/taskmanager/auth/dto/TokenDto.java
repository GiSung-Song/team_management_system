package com.manager.taskmanager.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토큰 Response DTO")
public class TokenDto {

    @Schema(name = "Access Token")
    private String accessToken;

    @Schema(name = "Refresh Token")
    private String refreshToken;
}
