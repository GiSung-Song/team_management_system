package com.manager.taskmanager.member;

import com.manager.taskmanager.config.DBContainerExtension;
import com.manager.taskmanager.config.security.CustomUserDetails;
import com.manager.taskmanager.department.DepartmentRepository;
import com.manager.taskmanager.department.entity.Department;
import com.manager.taskmanager.member.dto.MemberRegisterDto;
import com.manager.taskmanager.member.dto.MemberUpdateDto;
import com.manager.taskmanager.member.dto.PasswordUpdateDto;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.member.entity.Position;
import com.manager.taskmanager.member.entity.Role;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@ExtendWith(DBContainerExtension.class)
@AutoConfigureMockMvc
public class MemberIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member hrMember;
    private Member hrDeletedMember;
    private Member dvMember;
    private Member dvDeletedMember;

    private Department hrDepartment;
    private Department dvDepartment;

    @BeforeEach
    void setUp() {
        setDepartment();
        departmentRepository.saveAllAndFlush(List.of(hrDepartment, dvDepartment));

        setMember();
        memberRepository.saveAllAndFlush(List.of(hrMember, hrDeletedMember, dvMember, dvDeletedMember));
    }

    @AfterEach
    void clear() {
        clearAuthentication();
    }

    @Nested
    @DisplayName("회원 목록 조회 API")
    class 회원_목록_조회_테스트 {

        @Test
        @DisplayName("회원 목록 정상 조회 - ROLE_MEMBER")
        void whenValidInput_thenReturnMemberListForMember() throws Exception {
            setAuthentication(100L, Role.MEMBER);

            mockMvc.perform(get("/api/member"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.data.size()").value(2))
                    .andExpect(jsonPath("$.data[*].name", containsInAnyOrder("hrMb", "dvMb")));
        }

        @Test
        @DisplayName("회원 목록 정상 조회 - ROLE_MANAGER")
        void whenValidInput_thenReturnMemberListForManager() throws Exception {
            setAuthentication(100L, Role.MANAGER);

            mockMvc.perform(get("/api/member"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.data.size()").value(4))
                    .andExpect(jsonPath("$.data[*].name", containsInAnyOrder("hrMb", "dvMb", "hrDtMb", "dvDtMb")));
        }

        @Test
        @DisplayName("인증 실패 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/member"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("회원 가입 API")
    class 회원_가입_테스트 {

        @Test
        @DisplayName("회원 정상 가입")
        void whenValidInput_thenMemberIsCreated() throws Exception {
            MemberRegisterDto memberRegisterDto = new MemberRegisterDto(
                    "emp-0005", "rawPassword", "tester5", "tester5@email.com",
                    "01013572468", Position.DEPARTMENT_HEAD.name(), dvDepartment.getId()
            );

            mockMvc.perform(post("/api/member")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(memberRegisterDto)))
                    .andExpect(status().isCreated())
                    .andDo(print());

            boolean existsByEmail = memberRepository.existsByEmail(memberRegisterDto.getEmail());

            assertThat(existsByEmail).isTrue();
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenInValidInput_thenReturnBadRequest() throws Exception {
            MemberRegisterDto memberRegisterDto = new MemberRegisterDto(
                    "emp-0005", "rawPassword", "tester5", "tester5@email.com",
                    "01013572468", Position.DEPARTMENT_HEAD.name(), null
            );

            mockMvc.perform(post("/api/member")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(memberRegisterDto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("없는 부서 입력 시 404 반환")
        void whenInValidDepartment_thenReturnNotFound() throws Exception {
            MemberRegisterDto memberRegisterDto = new MemberRegisterDto(
                    "emp-0005", "rawPassword", "tester5", "tester5@email.com",
                    "01013572468", Position.DEPARTMENT_HEAD.name(), 123123L
            );

            mockMvc.perform(post("/api/member")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(memberRegisterDto)))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        @DisplayName("중복된 값(사번/이메일/휴대폰 번호) 입력 시 409 반환")
        void whenInputDuplicatedValid_thenReturnConflict() throws Exception {
            MemberRegisterDto memberRegisterDto = new MemberRegisterDto(
                    "emp-0004", "rawPassword", "tester5", "tester5@email.com",
                    "01013572468", Position.DEPARTMENT_HEAD.name(), dvDepartment.getId()
            );

            mockMvc.perform(post("/api/member")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(memberRegisterDto)))
                    .andExpect(status().isConflict())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("회원 정보 수정 API")
    class 회원_정보_수정_테스트 {

        @Test
        @DisplayName("회원 정보 정상 수정 - MANAGER")
        @WithMockUser(roles = "MANAGER")
        void whenValidInput_thenMemberIsUpdatedForManager() throws Exception {
            MemberUpdateDto dto = new MemberUpdateDto(
                    "test12345@email.com", "01123234545", Position.STAFF.name(), hrDepartment.getId()
            );

            mockMvc.perform(patch("/api/member/{memberId}", hrMember.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andDo(print());

            Member member = memberRepository.findById(hrMember.getId()).orElseThrow();

            assertThat(member.getEmail()).isEqualTo(dto.getEmail());
            assertThat(member.getPhoneNumber()).isEqualTo(dto.getPhoneNumber());
            assertThat(member.getPosition().name()).isEqualTo(dto.getPosition());
            assertThat(member.getDepartment().getId()).isEqualTo(dto.getDepartmentId());
        }

        @Test
        @DisplayName("회원 정보 정상 수정 - 주인")
        void whenValidInput_thenMemberIsUpdatedForOwner() throws Exception {
            Member member = Member.builder()
                    .employeeNumber("emp-0005")
                    .password(passwordEncoder.encode("password"))
                    .name("member")
                    .email("member@email.com")
                    .phoneNumber("01724382034")
                    .position(Position.STAFF)
                    .department(dvDepartment)
                    .role(Role.MEMBER)
                    .build();

            memberRepository.saveAndFlush(member);

            setAuthentication(member.getId(), Role.MEMBER);

            MemberUpdateDto dto = new MemberUpdateDto(
                    "test12345@email.com", "01123234545", Position.STAFF.name(), hrDepartment.getId()
            );

            mockMvc.perform(patch("/api/member/{memberId}", member.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andDo(print());

            Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();

            assertThat(updatedMember.getEmail()).isEqualTo(dto.getEmail());
            assertThat(updatedMember.getPhoneNumber()).isEqualTo(dto.getPhoneNumber());
            assertThat(updatedMember.getPosition().name()).isEqualTo(dto.getPosition());
            assertThat(updatedMember.getDepartment().getId()).isEqualTo(dto.getDepartmentId());
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenInValidInput_thenReturnBadRequest() throws Exception {
            setAuthentication(hrMember.getId(), Role.MEMBER);

            MemberUpdateDto dto = new MemberUpdateDto(
                    "test12345@email.com", "01123234545", Position.STAFF.name(), null
            );

            mockMvc.perform(patch("/api/member/{memberId}", hrMember.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("비인증 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            MemberUpdateDto dto = new MemberUpdateDto(
                    "test12345@email.com", "01123234545", Position.STAFF.name(), hrDepartment.getId()
            );

            mockMvc.perform(patch("/api/member/{memberId}", hrMember.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("권한 없을 시 403 반환")
        void whenUnauthorized_thenReturnForbidden() throws Exception {
            setAuthentication(dvMember.getId(), Role.MEMBER);

            MemberUpdateDto dto = new MemberUpdateDto(
                    "test12345@email.com", "01123234545", Position.STAFF.name(), hrDepartment.getId()
            );

            mockMvc.perform(patch("/api/member/{memberId}", hrMember.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(dto)))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("없는 부서 입력 시 404 반환")
        void whenInValidDepartment_thenReturnNotFound() throws Exception {
            setAuthentication(dvMember.getId(), Role.MEMBER);

            MemberUpdateDto dto = new MemberUpdateDto(
                    "test12345@email.com", "01123234545", Position.STAFF.name(), 3421432L
            );

            mockMvc.perform(patch("/api/member/{memberId}", dvMember.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(dto)))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        @DisplayName("중복된 값(사번/이메일/휴대폰 번호) 입력 시 409 반환")
        void whenInputDuplicatedValid_thenReturnConflict() throws Exception {
            setAuthentication(dvMember.getId(), Role.MEMBER);

            MemberUpdateDto dto = new MemberUpdateDto(
                    hrMember.getEmail(), "01123234545", Position.STAFF.name(), dvDepartment.getId()
            );

            mockMvc.perform(patch("/api/member/{memberId}", dvMember.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(dto)))
                    .andExpect(status().isConflict())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("회원 비밀번호 수정 API")
    class 회원_비밀번호_수정_테스트 {

        @Test
        @DisplayName("회원 비밀번호 정상 수정")
        void whenValidInput_thenPasswordIsUpdated() throws Exception {
            setAuthentication(dvMember.getId(), Role.MEMBER);

            PasswordUpdateDto newPassword = new PasswordUpdateDto("newPassword");

            mockMvc.perform(patch("/api/member/{memberId}/password", dvMember.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(newPassword)))
                    .andExpect(status().isOk())
                    .andDo(print());

            Member updatedMember = memberRepository.findById(dvMember.getId()).orElseThrow();

            assertThat(passwordEncoder.matches(newPassword.getPassword(), updatedMember.getPassword())).isTrue();
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenInValidInput_thenReturnBadRequest() throws Exception {
            setAuthentication(dvMember.getId(), Role.MEMBER);

            PasswordUpdateDto newPassword = new PasswordUpdateDto();

            mockMvc.perform(patch("/api/member/{memberId}/password", dvMember.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(newPassword)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("비인증 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            PasswordUpdateDto newPassword = new PasswordUpdateDto("newPassword");

            mockMvc.perform(patch("/api/member/{memberId}/password", dvMember.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(newPassword)))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("권한 없을 시 403 반환")
        void whenUnauthorized_thenReturnForbidden() throws Exception {
            setAuthentication(hrMember.getId(), Role.MEMBER);

            PasswordUpdateDto newPassword = new PasswordUpdateDto("newPassword");

            mockMvc.perform(patch("/api/member/{memberId}/password", dvMember.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(newPassword)))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("회원 비밀번호 초기화 API")
    class 회원_비밀번호_초기화_테스트 {

        @Test
        @DisplayName("회원 비밀번호 정상 초기화")
        @WithMockUser(roles = "MANAGER")
        void whenValidInput_thenPasswordIsReset() throws Exception {
            mockMvc.perform(post("/api/member/{memberId}/password/reset", dvMember.getId()))
                    .andExpect(status().isOk())
                    .andDo(print());

            Member member = memberRepository.findById(dvMember.getId()).orElseThrow();

            assertThat(passwordEncoder.matches("password", member.getPassword())).isFalse();
        }

        @Test
        @DisplayName("잘못된 요청 시 400 반환")
        @WithMockUser(roles = "MANAGER")
        void whenInValidInput_thenReturnBadRequest() throws Exception {
            mockMvc.perform(post("/api/member/{memberId}/password/reset", "member"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("비인증 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            mockMvc.perform(post("/api/member/{memberId}/password/reset", dvMember.getId()))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("권한 없을 시 403 반환")
        @WithMockUser(roles = "MEMBER")
        void whenUnauthorized_thenReturnForbidden() throws Exception {
            mockMvc.perform(post("/api/member/{memberId}/password/reset", dvMember.getId()))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("회원 번호 없을 시 404 반환")
        @WithMockUser(roles = "MANAGER")
        void whenInvalidMemberId_thenReturnNotFound() throws Exception {
            mockMvc.perform(post("/api/member/{memberId}/password/reset", 5432543L))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("회원 삭제 API")
    class 회원_삭제_테스트 {

        @Test
        @DisplayName("회원 정상 삭제")
        @WithMockUser(roles = "MANAGER")
        void whenValidInput_thenMemberIsDeleted() throws Exception {
            mockMvc.perform(delete("/api/member/{memberId}", dvMember.getId()))
                    .andExpect(status().isOk())
                    .andDo(print());

            memberRepository.flush();

            Member member = memberRepository.findById(dvMember.getId()).orElseThrow();

            assertThat(member.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("잘못된 요청 시 400 반환")
        @WithMockUser(roles = "MANAGER")
        void whenInValidInput_thenReturnBadRequest() throws Exception {
            mockMvc.perform(delete("/api/member/{memberId}", "member"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("비인증 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            mockMvc.perform(delete("/api/member/{memberId}", dvMember.getId()))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("권한 없을 시 403 반환")
        @WithMockUser(roles = "MEMBER")
        void whenUnauthorized_thenReturnForbidden() throws Exception {
            mockMvc.perform(delete("/api/member/{memberId}", dvMember.getId()))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Test
        @DisplayName("회원 번호 없을 시 404 반환")
        @WithMockUser(roles = "MANAGER")
        void whenInvalidMemberId_thenReturnNotFound() throws Exception {
            mockMvc.perform(delete("/api/member/{memberId}", 5432543L))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("회원 정보 상세 조회 API")
    class 회원_정보_상세_조회_테스트 {

        @Test
        @DisplayName("회원 정보 상세 조회 - 일반")
        void whenValidInput_thenReturnMemberInfoForMember() throws Exception {
            setAuthentication(999L, Role.MEMBER);

            mockMvc.perform(get("/api/member/{memberId}", hrMember.getId()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.data.employeeNumber").value(hrMember.getEmployeeNumber()))
                    .andExpect(jsonPath("$.data.email").value(hrMember.getEmail()))
                    .andExpect(jsonPath("$.data.phoneNumber").value(hrMember.getPhoneNumber()));
        }

        @Test
        @DisplayName("삭제된 회원 정보 상세 조회 시 404 반환")
        void whenInputDeletedMember_thenReturnNotFoundForMember() throws Exception {
            setAuthentication(999L, Role.MEMBER);

            mockMvc.perform(get("/api/member/{memberId}", hrDeletedMember.getId()))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        @DisplayName("회원 정보 상세 조회 - 관리자")
        @WithMockUser(roles = "MANAGER")
        void whenValidInput_thenReturnMemberInfoForManager() throws Exception {
            setAuthentication(999L, Role.MANAGER);

            mockMvc.perform(get("/api/member/{memberId}", hrMember.getId()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.data.employeeNumber").value(hrMember.getEmployeeNumber()))
                    .andExpect(jsonPath("$.data.email").value(hrMember.getEmail()))
                    .andExpect(jsonPath("$.data.phoneNumber").value(hrMember.getPhoneNumber()));
        }

        @Test
        @DisplayName("삭제된 회원 정보 상세 조회 - 관리자")
        @WithMockUser(roles = "MANAGER")
        void whenInputDeletedMember_thenReturnMemberInfoForManager() throws Exception {
            setAuthentication(999L, Role.MANAGER);

            mockMvc.perform(get("/api/member/{memberId}", hrDeletedMember.getId()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.data.employeeNumber").value(hrDeletedMember.getEmployeeNumber()))
                    .andExpect(jsonPath("$.data.email").value(hrDeletedMember.getEmail()))
                    .andExpect(jsonPath("$.data.phoneNumber").value(hrDeletedMember.getPhoneNumber()))
                    .andExpect(jsonPath("$.data.deletedAt").isNotEmpty());
        }

        @Test
        @DisplayName("잘못된 요청 시 400 반환")
        void whenInValidInput_thenReturnBadRequest() throws Exception {
            setAuthentication(999L, Role.MANAGER);

            mockMvc.perform(get("/api/member/{memberId}", "member"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("비 로그인 시 401 반환")
        void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/member/{memberId}", "member"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("회원 번호 없을 시 404 반환")
        void whenInvalidMemberId_thenReturnNotFound() throws Exception {
            setAuthentication(999L, Role.MEMBER);

            mockMvc.perform(get("/api/member/{memberId}", 5432543L))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    private void setAuthentication(Long id, Role role) {
        CustomUserDetails customUserDetails = new CustomUserDetails(id, "tester", role.getValue());

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                null,
                List.of(new SimpleGrantedAuthority(role.getValue()))
        );

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void setDepartment() {
        hrDepartment = Department.builder()
                .departmentName("hr")
                .build();

        dvDepartment = Department.builder()
                .departmentName("dv")
                .build();
    }

    private void setMember() {
        dvMember = Member.builder()
                .employeeNumber("emp-0001")
                .password(passwordEncoder.encode("password"))
                .name("dvMb")
                .email("dvMember@email.com")
                .phoneNumber("01012341234")
                .position(Position.STAFF)
                .department(dvDepartment)
                .role(Role.MEMBER)
                .build();

        dvDeletedMember = Member.builder()
                .employeeNumber("emp-0002")
                .password(passwordEncoder.encode("password"))
                .name("dvDtMb")
                .email("dvDeletedMember@email.com")
                .phoneNumber("01012344321")
                .position(Position.GENERAL_MANAGER)
                .department(dvDepartment)
                .role(Role.MEMBER)
                .deletedAt(LocalDateTime.now())
                .build();

        hrMember = Member.builder()
                .employeeNumber("emp-0003")
                .password(passwordEncoder.encode("password"))
                .name("hrMb")
                .email("hrMember@email.com")
                .phoneNumber("01056785678")
                .position(Position.ASSISTANT_MANAGER)
                .department(hrDepartment)
                .role(Role.MEMBER)
                .build();

        hrDeletedMember = Member.builder()
                .employeeNumber("emp-0004")
                .password(passwordEncoder.encode("password"))
                .name("hrDtMb")
                .email("hrDeletedMember@email.com")
                .phoneNumber("01056788765")
                .position(Position.EXECUTIVE_VICE_PRESIDENT)
                .department(hrDepartment)
                .role(Role.MEMBER)
                .deletedAt(LocalDateTime.now())
                .build();
    }
}
