package com.manager.taskmanager.auth;

import com.manager.taskmanager.auth.dto.LoginRequestDto;
import com.manager.taskmanager.auth.dto.TokenDto;
import com.manager.taskmanager.global.error.CustomException;
import com.manager.taskmanager.global.error.ErrorCode;
import com.manager.taskmanager.config.DBContainerExtension;
import com.manager.taskmanager.global.config.security.JwtPayloadDto;
import com.manager.taskmanager.global.config.security.JwtTokenUtil;
import com.manager.taskmanager.department.DepartmentRepository;
import com.manager.taskmanager.department.entity.Department;
import com.manager.taskmanager.member.MemberRepository;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.member.entity.Position;
import com.manager.taskmanager.member.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@ExtendWith(DBContainerExtension.class)
class AuthServiceTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private Member member;
    private Department department;

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
    }

    @Nested
    @DisplayName("로그인 Service")
    class 로그인_서비스_테스트 {

        @Test
        @DisplayName("정상 로그인")
        void whenValidInput_thenLoginSuccessfully() {
            LoginRequestDto loginRequestDto = new LoginRequestDto("emp-0003", "password");

            TokenDto tokenDto = authService.login(loginRequestDto);

            JwtPayloadDto jwtPayloadDto = jwtTokenUtil.parseAccessToken(tokenDto.getAccessToken());
            String refreshToken = redisTemplate.opsForValue().get("refresh:" + jwtPayloadDto.getId());

            assertThat(jwtPayloadDto.getEmployeeNumber()).isEqualTo("emp-0003");
            assertThat(tokenDto.getRefreshToken()).isEqualTo(refreshToken);
        }

        @Test
        @DisplayName("잘못된 사번 입력 시 401 반환")
        void whenInvalidEmployeeNumber_thenReturnUnauthorized() {
            LoginRequestDto loginRequestDto = new LoginRequestDto("emp-0004", "password");

            assertThatThrownBy(() -> authService.login(loginRequestDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }

        @Test
        @DisplayName("잘못된 비밀번호 입력 시 401 반환")
        void whenInvalidPassword_thenReturnUnauthorized() {
            LoginRequestDto loginRequestDto = new LoginRequestDto("emp-0003", "invalidPassword");

            assertThatThrownBy(() -> authService.login(loginRequestDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }
    }

    @Nested
    @DisplayName("로그아웃 Service")
    class 로그아웃_서비스_테스트 {

        @Test
        @DisplayName("정상 로그아웃")
        void whenValidToken_thenLogoutSuccessfully() {
            JwtPayloadDto jwtPayloadDto = new JwtPayloadDto(
                    member.getId(), department.getDepartmentName(), member.getEmployeeNumber(),
                    member.getRole().getValue(), member.getPosition()
            );

            String accessToken = jwtTokenUtil.generateAccessToken(jwtPayloadDto);

            authService.logout(accessToken);

            String hashToken = jwtTokenUtil.tokenToHash(accessToken);

            assertThat(redisTemplate.opsForValue().get(hashToken)).isNotNull();
        }

        @Test
        @DisplayName("Access 토큰 오류 시 401 반환")
        void whenAccessTokenIsNull_thenReturnUnauthorized() {
            assertThatThrownBy(() -> authService.logout("fdasjfoisdjafoijsod.fdsafdas.fsda"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }
    }

    @Nested
    @DisplayName("토큰 재발급 Service")
    class 토큰_재발급_서비스_테스트 {

        @Test
        @DisplayName("토큰 재발급 정상")
        void whenValidInput_thenReturnAccessToken() {
            JwtPayloadDto jwtPayloadDto = new JwtPayloadDto(
                    member.getId(), department.getDepartmentName(), member.getEmployeeNumber(),
                    member.getRole().getValue(), member.getPosition()
            );

            String refreshToken = jwtTokenUtil.generateRefreshToken(jwtPayloadDto);

            redisTemplate.opsForValue().set("refresh:" + member.getId(), refreshToken);

            TokenDto tokenDto = authService.reIssueToken(refreshToken);

            JwtPayloadDto parse = jwtTokenUtil.parseAccessToken(tokenDto.getAccessToken());

            assertThat(parse.getEmployeeNumber()).isEqualTo(member.getEmployeeNumber());
        }

        @Test
        @DisplayName("Refresh Token 없을 시 401 반환")
        void whenRefreshTokenIsNull_thenReturnUnauthorized() {
            assertThatThrownBy(() -> authService.reIssueToken(null))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }

        @Test
        @DisplayName("Refresh Token 유효하지 않을 시 401 반환")
        void whenInvalidRefreshToken_thenReturnUnauthorized() {
            assertThatThrownBy(() -> authService.reIssueToken("fdsafdsa.fdsafdsafdsa.fdsasd"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }

        @Test
        @DisplayName("회원이 없을 시 404 반환")
        void whenInvalidMember_thenReturnNotFound() {
            JwtPayloadDto jwtPayloadDto = new JwtPayloadDto(
                    member.getId(), department.getDepartmentName(), "emp-131325424",
                    member.getRole().getValue(), member.getPosition()
            );

            String refreshToken = jwtTokenUtil.generateRefreshToken(jwtPayloadDto);

            assertThatThrownBy(() -> authService.reIssueToken(refreshToken))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("저장된 토큰과 다를 시 401 반환")
        void whenAnotherRefreshToken_thenReturnUnauthorized() {
            JwtPayloadDto jwtPayloadDto = new JwtPayloadDto(
                    member.getId(), department.getDepartmentName(), member.getEmployeeNumber(),
                    member.getRole().getValue(), member.getPosition()
            );

            String refreshToken = jwtTokenUtil.generateRefreshToken(jwtPayloadDto);


            redisTemplate.opsForValue().set("refresh:" + member.getId(), refreshToken + "fdsafdsa");

            assertThatThrownBy(() -> authService.reIssueToken(refreshToken))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }
    }

}