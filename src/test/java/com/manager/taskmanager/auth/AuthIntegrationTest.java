package com.manager.taskmanager.auth;

import com.manager.taskmanager.auth.dto.LoginRequestDto;
import com.manager.taskmanager.config.DBContainerExtension;
import com.manager.taskmanager.config.security.JwtPayloadDto;
import com.manager.taskmanager.config.security.JwtTokenUtil;
import com.manager.taskmanager.department.DepartmentRepository;
import com.manager.taskmanager.department.entity.Department;
import com.manager.taskmanager.member.MemberRepository;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.member.entity.Position;
import com.manager.taskmanager.member.entity.Role;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@ExtendWith(DBContainerExtension.class)
@AutoConfigureMockMvc
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Member member;
    private Department department;
    private JwtPayloadDto jwtPayloadDto;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .departmentName("HR")
                .build();

        departmentRepository.saveAndFlush(department);

        member = Member.builder()
                .employeeNumber("emp-0003")
                .password(passwordEncoder.encode("password"))
                .name("hrMb")
                .email("hrMember@email.com")
                .phoneNumber("01056785678")
                .position(Position.ASSISTANT_MANAGER)
                .department(department)
                .role(Role.MEMBER)
                .build();

        memberRepository.saveAndFlush(member);

        jwtPayloadDto = new JwtPayloadDto(
                member.getId(), department.getDepartmentName(), member.getEmployeeNumber(),
                member.getRole().getValue(), member.getPosition()
        );
    }

    @Nested
    @DisplayName("로그인 API")
    class 로그인_테스트 {

        @Test
        @DisplayName("로그인 정상")
        void whenValidInput_thenLogin() throws Exception {
            LoginRequestDto loginRequestDto = new LoginRequestDto("emp-0003", "password");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(loginRequestDto)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(result -> {
                        String refreshToken = result.getResponse().getCookie("refreshToken").getValue();

                        assertThat(refreshToken).isNotNull();
                        assertThat(refreshToken).isNotEmpty();
                    });
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenMissingRequiredField_thenReturnBadRequest() throws Exception {
            LoginRequestDto loginRequestDto = new LoginRequestDto("emp-0003", null);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(loginRequestDto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("사번/비밀번호 틀릴 시 401 반환")
        void whenInvalidLoginField_thenReturnUnauthorized() throws Exception {
            LoginRequestDto loginRequestDto = new LoginRequestDto("emp-0003", "passwoword");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(loginRequestDto)))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("로그아웃 API")
    class 로그아웃_테스트 {

        @Test
        @DisplayName("로그아웃 정상")
        @WithMockUser(roles = "MEMBER")
        void whenValidToken_thenLogout() throws Exception {
            String accessToken = jwtTokenUtil.generateAccessToken(jwtPayloadDto);

            mockMvc.perform(post("/api/auth/logout")
                    .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andDo(print());

            String hashToken = jwtTokenUtil.tokenToHash(accessToken);

            String logout = redisTemplate.opsForValue().get(hashToken);

            assertThat(logout.equals("logout")).isTrue();
        }

        @Test
        @DisplayName("비인증 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("토큰 재발급 API")
    class 토큰_재발급_테스트 {

        @Test
        @DisplayName("토큰 재발급 정상")
        @WithMockUser(roles = "MEMBER")
        void whenValidToken_thenReturnAccessToken() throws Exception {
            String refreshToken = jwtTokenUtil.generateRefreshToken(jwtPayloadDto);
            redisTemplate.opsForValue().set("refresh:" + jwtPayloadDto.getId(), refreshToken);

            mockMvc.perform(post("/api/auth/reIssue")
                    .cookie(new Cookie("refreshToken", refreshToken)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
        }

        @Test
        @DisplayName("비인증 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            mockMvc.perform(post("/api/auth/reIssue"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("회원 없을 시 404 반환")
        @WithMockUser(roles = "MEMBER")
        void whenInvalidMember_thenReturnNotFound() throws Exception {
            jwtPayloadDto.setEmployeeNumber("emp-0001");
            String refreshToken = jwtTokenUtil.generateRefreshToken(jwtPayloadDto);

            mockMvc.perform(post("/api/auth/reIssue")
                            .cookie(new Cookie("refreshToken", refreshToken)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
}
