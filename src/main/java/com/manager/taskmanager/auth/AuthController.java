package com.manager.taskmanager.auth;

import com.manager.taskmanager.auth.dto.LoginRequestDto;
import com.manager.taskmanager.auth.dto.TokenDto;
import com.manager.taskmanager.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "로그인을 진행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(필수 입력 값 누락)"),
            @ApiResponse(responseCode = "401", description = "잘못된 요청(아이디/비밀번호)")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResult<TokenDto>> login(
            @RequestBody @Valid LoginRequestDto dto,
            HttpServletResponse response
    ) {
        TokenDto tokenDto = authService.login(dto);

        Cookie cookie = new Cookie("refreshToken", tokenDto.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(cookie);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "로그인 성공했습니다.", new TokenDto(tokenDto.getAccessToken(), null)));
    }

    @Operation(summary = "로그아웃", description = "로그아웃을 진행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "잘못된 요청(토큰 유효/만료/비인증)")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResult<Void>> logout(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String accessToken = authorizationHeader.substring(7);

        authService.logout(accessToken);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "로그아웃 성공했습니다.", null));
    }

    @Operation(summary = "Access Token 재발급", description = "Access Token을 재발급 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "잘못된 요청(토큰 유효/만료/비인증)"),
            @ApiResponse(responseCode = "404", description = "잘못된 요청(회원)")
    })
    @PostMapping("/reIssue")
    public ResponseEntity<ApiResult<TokenDto>> reIssueToken(
            @CookieValue("refreshToken") String refreshToken
    ) {
        TokenDto tokenDto = authService.reIssueToken(refreshToken);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "토큰을 재발급 했습니다.", tokenDto));
    }

}
