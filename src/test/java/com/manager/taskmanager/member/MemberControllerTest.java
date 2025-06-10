package com.manager.taskmanager.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.taskmanager.global.config.security.JwtTokenFilter;
import com.manager.taskmanager.member.dto.*;
import com.manager.taskmanager.member.entity.Position;
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

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = MemberController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class))
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @Nested
    @DisplayName("회원 목록 조회 Controller")
    class 회원_목록_조회_컨트롤러_테스트 {

        @Test
        @DisplayName("회원 목록 조회 정상")
        void whenValidParam_thenReturnMemberList() throws Exception {
            MemberListDto tester1 = new MemberListDto(0L, "테스터1", Position.STAFF.name(), "hr");
            MemberListDto tester2 = new MemberListDto(1L, "테스터2", Position.STAFF.name(), "hr");
            List<MemberListDto> memberListDtoList = List.of(tester1, tester2);

            given(memberService.getMemberList("hr", "", false)).willReturn(memberListDtoList);
            mockMvc.perform(get("/api/members")
                            .param("departmentName", "hr"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.data[0].name").value("테스터1"))
                    .andExpect(jsonPath("$.data[1].name").value("테스터2"));
        }
    }

    @Nested
    @DisplayName("회원가입 Controller")
    class 회원가입_컨트롤러_테스트 {

        @Test
        @DisplayName("회원가입 정상")
        void whenValidInput_thenMemberIsCreated() throws Exception {
            MemberRegisterDto dto = new MemberRegisterDto(
                    "emp-0001", "rawPassword", "tester1", "email@email.com",
                    "01012344321", "STAFF", 1L
            );

            willDoNothing().given(memberService).registerMember(dto);

            mockMvc.perform(post("/api/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andDo(print());
        }

        @Test
        @DisplayName("필수 값 누락 400 반환")
        void whenMissingRequiredField_thenReturnBadRequest() throws Exception {
            MemberRegisterDto dto = new MemberRegisterDto(
                    "emp-0001", null, "tester1", "email@email.com",
                    "01012344321", "STAFF", 1L);

                    mockMvc.perform(post("/api/members")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(dto)))
                            .andExpect(status().isBadRequest())
                            .andDo(print());
        }
    }

    @Nested
    @DisplayName("회원 정보 수정 Controller")
    class 회원_정보_수정_컨트롤러_테스트 {

        @Test
        @DisplayName("회원 정보 정상 수정")
        void whenValidInput_thenMemberIsUpdated() throws Exception {
            MemberUpdateDto intern = new MemberUpdateDto(
                    "email@email.com", "01012341234",
                    "INTERN", 1L
            );

            willDoNothing().given(memberService).updateMember(0L, intern);

            mockMvc.perform(patch("/api/members/{memberId}", 0L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(intern)))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenInValidInput_thenReturnBadRequest() throws Exception {
            MemberUpdateDto intern = new MemberUpdateDto(
                    "email@email.com", "01012341234",
                    "INTERN", null
            );

            mockMvc.perform(patch("/api/members/{memberId}", 0L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(intern)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("회원 비밀번호 수정 Controller")
    class 회원_비밀번호_수정_컨트롤러_테스트 {

        @Test
        @DisplayName("회원 비밀번호 정상 수정")
        void whenValidInput_thenMemberPasswordIsUpdated() throws Exception {
            PasswordUpdateDto dto = new PasswordUpdateDto("newPassword");

            willDoNothing().given(memberService).updatePassword(0L, dto);

            mockMvc.perform(patch("/api/members/{memberId}/password", 0L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        @DisplayName("필수 값 누락 시 400 반환")
        void whenInValidInput_thenReturnBadRequest() throws Exception {
            PasswordUpdateDto dto = new PasswordUpdateDto();

            mockMvc.perform(patch("/api/members/{memberId}/password", 0L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("회원 비밀번호 초기화 Controller")
    class 회원_비밀번호_초기화_컨트롤러_테스트 {

        @Test
        @DisplayName("회원 비밀번호 정상 초기화")
        void whenValidInput_thenMemberPasswordIsReset() throws Exception {
            willDoNothing().given(memberService).resetPassword(0L);

            mockMvc.perform(post("/api/members/{memberId}/password/reset", 0L))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        @DisplayName("변환 불가능한 path 요청 시 400 반환")
        void whenInValidPath_thenReturnBadRequest() throws Exception {
            mockMvc.perform(post("/api/members/{memberId}/password/reset", "test"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("회원 삭제 Controller")
    class 회원_삭제_컨트롤러_테스트 {

        @Test
        @DisplayName("회원 정상 삭제")
        void whenValidInput_thenMemberIsDeleted() throws Exception {
            willDoNothing().given(memberService).deleteMember(0L);

            mockMvc.perform(delete("/api/members/{memberId}", 0L))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        @DisplayName("변환 불가능한 path 요청 시 400 반환")
        void whenInValidPath_thenReturnBadRequest() throws Exception {
            mockMvc.perform(delete("/api/members/{memberId}", "test"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("회원 정보 상세 조회 Controller")
    class 회원_정보_상세_조회_컨트롤러_테스트 {

        @Test
        @DisplayName("회원 상세 정보 정상 조회")
        void whenValidInput_thenReturnMemberInfo() throws Exception {
            MemberResponseDto memberResponseDto = new MemberResponseDto(
                    2L, "emp-0002", "tester2", "email@email.com", "01013132424",
                    MemberResponseDto.PositionDto.from(Position.DIRECTOR), "HR", null
            );

            given(memberService.getMemberById(2L)).willReturn(memberResponseDto);

            mockMvc.perform(get("/api/members/{memberId}", 2L))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.data.employeeNumber").value(memberResponseDto.getEmployeeNumber()))
                    .andExpect(jsonPath("$.data.email").value(memberResponseDto.getEmail()))
                    .andExpect(jsonPath("$.data.phoneNumber").value(memberResponseDto.getPhoneNumber()));
        }

        @Test
        @DisplayName("변환 불가능한 path 요청 시 400 반환")
        void whenInValidPath_thenReturnBadRequest() throws Exception {
            mockMvc.perform(get("/api/members/{memberId}", "test"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }
}