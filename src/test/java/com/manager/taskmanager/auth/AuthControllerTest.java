package com.manager.taskmanager.auth;

import com.manager.taskmanager.auth.dto.LoginRequestDto;
import com.manager.taskmanager.auth.dto.TokenDto;
import com.manager.taskmanager.common.CustomException;
import com.manager.taskmanager.common.ErrorCode;
import com.manager.taskmanager.config.security.JwtTokenFilter;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class))
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Nested
    @DisplayName("로그인 Controller")
    class 로그인_컨트롤러_테스트 {

        @Test
        @DisplayName("로그인 정상")
        void whenValidInput_thenLogin() throws Exception {
            LoginRequestDto dto = new LoginRequestDto("emp-0001", "password");
            TokenDto tokenDto = new TokenDto("accessToken", "refreshToken");

            given(authService.login(any(LoginRequestDto.class))).willReturn(tokenDto);

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(cookie().value("refreshToken", tokenDto.getRefreshToken()))
                    .andExpect(jsonPath("$.data.accessToken").value(tokenDto.getAccessToken()))
                    .andExpect(jsonPath("$.data.refreshToken").isEmpty());
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenMissingRequiredField_thenReturnBadRequest() throws Exception {
            LoginRequestDto dto = new LoginRequestDto("emp-0001", null);

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("사번/비밀번호 틀릴 시 401 반환")
        void whenInvalidLoginField_thenReturnUnauthorized() throws Exception {
            LoginRequestDto dto = new LoginRequestDto("emp-0001", "password");
            willThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS)).given(authService).login(any(LoginRequestDto.class));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("로그아웃 Controller")
    class 로그아웃_컨트롤러_테스트 {

        @Test
        @DisplayName("로그아웃 정상")
        void whenValidToken_thenLogout() throws Exception {
            willDoNothing().given(authService).logout(anyString());

            mockMvc.perform(post("/api/auth/logout")
                    .header("Authorization", "Bearer accessToken"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("잘못된 토큰 시 401 반환")
        void whenInvalidToken_thenReturnUnauthorized() throws Exception {
            willThrow(new CustomException(ErrorCode.INVALID_TOKEN)).given(authService).logout(anyString());

            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer accessToken"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("토큰 재발급 Controller")
    class 토큰_재발급_컨트롤러_테스트 {

        @Test
        @DisplayName("토큰 재발급 정상")
        void whenTokenValid_thenReturnAccessToken() throws Exception {
            TokenDto tokenDto = new TokenDto("accessToken", null);

            given(authService.reIssueToken(anyString())).willReturn(tokenDto);

            mockMvc.perform(post("/api/auth/reIssue")
                    .cookie(new Cookie("refreshToken", "refreshToken")))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        @DisplayName("잘못된 토큰 시 401 반환")
        void whenInvalidToken_thenReturnUnauthorized() throws Exception {
            willThrow(new CustomException(ErrorCode.INVALID_TOKEN)).given(authService).reIssueToken(anyString());

            mockMvc.perform(post("/api/auth/reIssue")
                            .cookie(new Cookie("refreshToken", "AAFDAFDAFDSA")))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("회원 없을 시 404 반환")
        void whenInvalidMember_thenReturnNotfound() throws Exception {
            willThrow(new CustomException(ErrorCode.MEMBER_NOT_FOUND)).given(authService).reIssueToken(anyString());

            mockMvc.perform(post("/api/auth/reIssue")
                            .cookie(new Cookie("refreshToken", "refreshToken")))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }
}