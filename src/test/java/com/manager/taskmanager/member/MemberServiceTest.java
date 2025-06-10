package com.manager.taskmanager.member;

import com.manager.taskmanager.global.error.CustomException;
import com.manager.taskmanager.global.error.ErrorCode;
import com.manager.taskmanager.config.DBContainerExtension;
import com.manager.taskmanager.department.DepartmentRepository;
import com.manager.taskmanager.department.entity.Department;
import com.manager.taskmanager.member.dto.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@ExtendWith(DBContainerExtension.class)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    Member dvMember;
    Member hrMember;
    Department hr;
    Department dv;
    Department ca;

    @BeforeEach
    void setUp() {
        hr = Department.builder()
                .departmentName("HR")
                .build();

        dv = Department.builder()
                .departmentName("DV")
                .build();

        ca = Department.builder()
                .departmentName("CA")
                .build();

        List<Department> list = List.of(hr, dv, ca);

        departmentRepository.saveAll(list);

        dvMember = Member.builder()
                .employeeNumber("emp-0001")
                .password(passwordEncoder.encode("password"))
                .name("active")
                .email("active@email.com")
                .phoneNumber("01012341234")
                .position(Position.STAFF)
                .department(dv)
                .role(Role.MEMBER)
                .build();

        hrMember = Member.builder()
                .employeeNumber("emp-0011")
                .password(passwordEncoder.encode("password"))
                .name("active")
                .email("hrMember@email.com")
                .phoneNumber("01012345678")
                .position(Position.GENERAL_MANAGER)
                .department(hr)
                .role(Role.MEMBER)
                .build();

        memberRepository.save(dvMember);
        memberRepository.save(hrMember);

        memberRepository.flush();
        departmentRepository.flush();
    }

    @Nested
    @DisplayName("회원 가입 Service")
    class 회원가입_서비스_테스트 {

        @Test
        @DisplayName("정상 가입")
        void whenValidInput_thenMemberIsCreated() {
            MemberRegisterDto dto = new MemberRegisterDto(
                    "EMP-0002", "rawPassword", "tester", "email@email.com",
                    "01013132424", "INTERN", hr.getId()
            );

            memberService.registerMember(dto);

            Member findMember = findByEmail(dto.getEmail());

            assertThat(findMember.getName()).isEqualTo(dto.getName());
            assertThat(findMember.getEmail()).isEqualTo(dto.getEmail());
            assertThat(findMember.getEmployeeNumber()).isEqualTo(dto.getEmployeeNumber());
            assertThat(findMember.getPhoneNumber()).isEqualTo(dto.getPhoneNumber());
        }

        @Test
        @DisplayName("사번 > 중복 값 입력 시 409 반환")
        void whenEmployeeNumberIsDuplicated_thenReturnConflict() {
            MemberRegisterDto dto = new MemberRegisterDto(
                    dvMember.getEmployeeNumber(), "rawPassword", "tester", "email@email.com",
                    "01013132424", "INTERN", hr.getId()
            );

            assertThatThrownBy(() -> memberService.registerMember(dto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_NUMBER_DUPLICATE);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }

        @Test
        @DisplayName("이메일 > 중복 값 입력 시 409 반환")
        void whenEmailIsDuplicated_thenReturnConflict() {
            MemberRegisterDto dto = new MemberRegisterDto(
                    "emp-0002", "rawPassword", "tester", dvMember.getEmail(),
                    "01013132424", "INTERN", hr.getId()
            );

            assertThatThrownBy(() -> memberService.registerMember(dto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMAIL_DUPLICATE);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }

        @Test
        @DisplayName("휴대폰 번호 > 중복 값 입력 시 409 반환")
        void whenPhoneNumberIsDuplicated_thenReturnConflict() {
            MemberRegisterDto dto = new MemberRegisterDto(
                    "emp-0002", "rawPassword", "tester", "email@email.com",
                    dvMember.getPhoneNumber(), "INTERN", hr.getId()
            );

            assertThatThrownBy(() -> memberService.registerMember(dto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PHONE_NUMBER_DUPLICATE);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }

        @Test
        @DisplayName("없는 부서 번호 입력 시 404 반환")
        void whenInvalidDepartmentId_thenReturnNotFound() {
            MemberRegisterDto dto = new MemberRegisterDto(
                    "emp-0002", "rawPassword", "tester", "email@email.com",
                    "01013132424", "INTERN", 9999L
            );

            assertThatThrownBy(() -> memberService.registerMember(dto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DEPARTMENT_NOT_FOUND);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("회원 정보 수정 Service")
    class 회원_정보_수정_서비스_테스트 {

        @Test
        @DisplayName("정상 수정")
        void whenValidInput_thenMemberIsUpdated() {
            MemberUpdateDto updateDto = new MemberUpdateDto(
                    "email@email.com",
                    "01013132424",
                    "INTERN",
                    hr.getId()
            );

            memberService.updateMember(dvMember.getId(), updateDto);

            Member findMember = memberRepository.findById(dvMember.getId()).orElseThrow();

            assertThat(findMember.getEmail()).isEqualTo(updateDto.getEmail());
            assertThat(findMember.getPhoneNumber()).isEqualTo(updateDto.getPhoneNumber());
            assertThat(findMember.getPosition().name()).isEqualTo(updateDto.getPosition());
            assertThat(findMember.getDepartment().getId()).isEqualTo(updateDto.getDepartmentId());
        }

        @Test
        @DisplayName("이메일 > 중복 값 입력 시 409 반환")
        void whenEmailIsDuplicated_thenReturnConflict() {
            MemberUpdateDto updateDto = new MemberUpdateDto(
                    hrMember.getEmail(),
                    "01013132424",
                    "INTERN",
                    hr.getId()
            );

            assertThatThrownBy(() -> memberService.updateMember(dvMember.getId(), updateDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMAIL_DUPLICATE);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }

        @Test
        @DisplayName("휴대폰 번호 > 중복 값 입력 시 409 반환")
        void whenPhoneNumberIsDuplicated_thenReturnConflict() {
            MemberUpdateDto updateDto = new MemberUpdateDto(
                    "newMember@email.com",
                    hrMember.getPhoneNumber(),
                    "INTERN",
                    hr.getId()
            );

            assertThatThrownBy(() -> memberService.updateMember(dvMember.getId(), updateDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PHONE_NUMBER_DUPLICATE);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }

        @Test
        @DisplayName("없는 부서 번호 입력 시 404 반환")
        void whenInvalidDepartmentId_thenReturnNotFound() {
            MemberUpdateDto updateDto = new MemberUpdateDto(
                    "newMember@email.com",
                    "01079791414",
                    "INTERN",
                    9999L
            );

            assertThatThrownBy(() -> memberService.updateMember(dvMember.getId(), updateDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DEPARTMENT_NOT_FOUND);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("회원 비밀번호 수정 Service")
    class 회원_비밀번호_수정_서비스_테스트 {

        @Test
        @DisplayName("비밀번호 정상 수정")
        void whenValidInput_thenPasswordIsUpdated() {
            PasswordUpdateDto updateDto = new PasswordUpdateDto("newPassword");

            memberService.updatePassword(dvMember.getId(), updateDto);

            Member findMember = memberRepository.findById(dvMember.getId()).orElseThrow();

            assertThat(passwordEncoder.matches(updateDto.getPassword(), findMember.getPassword())).isTrue();
        }

        @Test
        @DisplayName("없는 회원 번호 입력 시 404 반환")
        void whenInvalidMemberId_thenReturnNotFound() {
            PasswordUpdateDto updateDto = new PasswordUpdateDto("newPassword");

            assertThatThrownBy(() -> memberService.updatePassword(9999L, updateDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("회원 비밀번호 초기화 Service")
    class 회원_비밀번호_초기화_서비스_테스트 {

        @Test
        @DisplayName("비밀번호 초기화 정상")
        void whenValidInput_thenPasswordIsReset() {
            memberService.resetPassword(dvMember.getId());

            Member findMember = memberRepository.findById(dvMember.getId()).orElseThrow();

            assertThat(passwordEncoder.matches("password", findMember.getPassword())).isFalse();
        }

        @Test
        @DisplayName("없는 회원 번호 입력 시 404 반환")
        void whenInvalidMemberId_thenReturnNotFound() {
            assertThatThrownBy(() -> memberService.resetPassword(9999L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("회원 삭제 Service")
    class 회원_삭제_서비스_테스트 {

        @Test
        @DisplayName("회원 삭제 정상")
        void whenValidInput_thenMemberIsDeleted() {
            assertThat(dvMember.getDeletedAt()).isNull();

            memberService.deleteMember(dvMember.getId());

            memberRepository.flush();

            Member findMember = memberRepository.findById(dvMember.getId()).orElseThrow();

            assertThat(findMember.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("없는 회원 번호 입력 시 404 반환")
        void whenInvalidMemberId_thenReturnNotFound() {
            assertThatThrownBy(() -> memberService.deleteMember(9999L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("회원정보 상세 조회(일반) Service")
    class 회원정보_상세_조회_서비스_테스트 {

        @Test
        @DisplayName("회원정보 정상 조회")
        void whenValidInput_thenReturnMemberInfo() {
            MemberResponseDto memberById = memberService.getMemberById(dvMember.getId());

            assertThat(memberById.getEmail()).isEqualTo(dvMember.getEmail());
            assertThat(memberById.getPhoneNumber()).isEqualTo(dvMember.getPhoneNumber());
            assertThat(memberById.getEmployeeNumber()).isEqualTo(dvMember.getEmployeeNumber());
        }

        @Test
        @DisplayName("없는 회원 번호 입력 시 404 반환")
        void whenInvalidMemberId_thenReturnNotFound() {
            assertThatThrownBy(() -> memberService.getMemberById(9999L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("삭제된 회원 번호 입력 시 404 반환")
        void whenDeletedMemberId_thenReturnNotFound() {
            memberService.deleteMember(dvMember.getId());
            memberRepository.flush();

            assertThatThrownBy(() -> memberService.getMemberById(dvMember.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("회원정보 상세 조회(관리자) Service")
    class 회원정보_상세_조회_관리자_서비스_테스트 {

        @Test
        @DisplayName("회원정보 정상 조회")
        void whenValidInput_thenReturnMemberInfo() {
            MemberResponseDto memberById = memberService.getMemberByIdForManager(dvMember.getId());

            assertThat(memberById.getEmail()).isEqualTo(dvMember.getEmail());
            assertThat(memberById.getPhoneNumber()).isEqualTo(dvMember.getPhoneNumber());
            assertThat(memberById.getEmployeeNumber()).isEqualTo(dvMember.getEmployeeNumber());
        }

        @Test
        @DisplayName("없는 회원 번호 입력 시 404 반환")
        void whenInvalidMemberId_thenReturnNotFound() {
            assertThatThrownBy(() -> memberService.getMemberByIdForManager(9999L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("삭제된 회원 번호 입력 시 정상 조회")
        void whenDeletedMemberId_thenReturnNotFound() {
            memberService.deleteMember(dvMember.getId());
            memberRepository.flush();

            MemberResponseDto memberByIdForManager = memberService.getMemberByIdForManager(dvMember.getId());

            assertThat(memberByIdForManager.getEmail()).isEqualTo(dvMember.getEmail());
            assertThat(memberByIdForManager.getPhoneNumber()).isEqualTo(dvMember.getPhoneNumber());
            assertThat(memberByIdForManager.getEmployeeNumber()).isEqualTo(dvMember.getEmployeeNumber());
            assertThat(memberByIdForManager.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("회원 목록 조회 Service")
    class 회원_목록_조회_테스트 {

        @Test
        @DisplayName("회원 목록 정상 조회 (일반)")
        void whenMemberValidInput_thenReturnMemberList() {
            memberService.deleteMember(dvMember.getId());
            memberRepository.flush();

            List<MemberListDto> memberList1 = memberService.getMemberList("", "", false);
            assertThat(memberList1.size()).isEqualTo(1);

            List<MemberListDto> memberList2 = memberService.getMemberList(hr.getDepartmentName(), "", false);
            assertThat(memberList2.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("회원 목록 정상 조회 (관리자)")
        void whenManagerValidInput_thenReturnMemberList() {
            memberService.deleteMember(dvMember.getId());
            memberRepository.flush();

            List<MemberListDto> memberList1 = memberService.getMemberList("", "", true);
            assertThat(memberList1.size()).isEqualTo(2);

            List<MemberListDto> memberList2 = memberService.getMemberList(hr.getDepartmentName(), "", true);
            assertThat(memberList2.size()).isEqualTo(1);
        }
    }

    private Member findByEmail(String email) {
        return memberRepository.findAll()
                .stream()
                .filter(m -> email.equals(m.getEmail()))
                .findFirst()
                .orElse(null);
    }
}