package com.manager.taskmanager.member;

import com.manager.taskmanager.common.ApiResult;
import com.manager.taskmanager.config.security.CustomUserDetails;
import com.manager.taskmanager.member.dto.*;
import com.manager.taskmanager.member.entity.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원 목록 조회", description = "회원 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자")
    })
    @GetMapping
    public ResponseEntity<ApiResult<List<MemberListDto>>> getAllMembers(
            @Parameter(description = "부서 이름 필터")
            @RequestParam(required = false, defaultValue = "") String departmentName,

            @Parameter(description = "회원 이름 필터")
            @RequestParam(required = false, defaultValue = "") String name,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        boolean isManager = Role.MANAGER.getValue().equals(member.getRole());

        List<MemberListDto> memberList = memberService.getMemberList(departmentName, name, isManager);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "회원 목록을 조회했습니다.", memberList));
    }

    @Operation(summary = "회원가입", description = "회원을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원 가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효성 검사 실패)"),
            @ApiResponse(responseCode = "404", description = "잘못된 요청(부서 없음)"),
            @ApiResponse(responseCode = "409", description = "잘못된 요청(사번/중복된 이메일/휴대폰 번호)")
    })
    @PostMapping
    public ResponseEntity<ApiResult<Void>> registerMember(@RequestBody @Valid MemberRegisterDto dto) {
        memberService.registerMember(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(HttpStatus.CREATED, "회원가입에 성공하였습니다.", null));
    }

    @Operation(summary = "회원 정보 수정", description = "회원 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효성 검사 실패)"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(관리자/사용자)"),
            @ApiResponse(responseCode = "404", description = "잘못된 요청(부서 없음/회원 정보 없음)"),
            @ApiResponse(responseCode = "409", description = "잘못된 요청(중복된 이메일/휴대폰 번호)")
    })
    @PreAuthorize("hasRole('MANAGER') or #memberId == principal.id")
    @PatchMapping("/{memberId}")
    public ResponseEntity<ApiResult<Void>> updateMember(
            @Parameter(description = "회원 ID", example = "1")
            @PathVariable("memberId") Long memberId,

            @RequestBody @Valid MemberUpdateDto dto
    ) {
        memberService.updateMember(memberId, dto);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "회원 정보를 수정했습니다.", null));
    }

    @Operation(summary = "회원 비밀번호 수정", description = "회원 비밀번호를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 비밀번호 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효성 검사 실패)"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(관리자/사용자)"),
            @ApiResponse(responseCode = "404", description = "잘못된 요청(회원 정보 없음)"),
    })
    @PreAuthorize("#memberId == principal.id")
    @PatchMapping("/{memberId}/password")
    public ResponseEntity<ApiResult<Void>> updateMemberPassword(
            @Parameter(description = "회원 ID", example = "1")
            @PathVariable("memberId") Long memberId,

            @RequestBody @Valid PasswordUpdateDto dto
    ) {
        memberService.updatePassword(memberId, dto);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "비밀번호를 수정했습니다.", null));
    }

    @Operation(summary = "회원 비밀번호 초기화", description = "회원 비밀번호를 초기화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 비밀번호 초기화 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(변환 불가능한 문자열)"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(관리자 전용)"),
            @ApiResponse(responseCode = "404", description = "잘못된 요청(회원 정보 없음)"),
    })
    @PostMapping("/{memberId}/password/reset")
    public ResponseEntity<ApiResult<Void>> resetMemberPassword(
            @Parameter(description = "회원 ID", example = "1")
            @PathVariable("memberId") Long memberId
    ) {
        memberService.resetPassword(memberId);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "비밀번호를 초기화했습니다.", null));
    }

    @Operation(summary = "회원 삭제", description = "회원을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(변환 불가능한 문자열)"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(관리자 전용)"),
            @ApiResponse(responseCode = "404", description = "잘못된 요청(회원 정보 없음)"),
    })
    @DeleteMapping("/{memberId}")
    public ResponseEntity<ApiResult<Void>> deleteMember(
            @Parameter(description = "회원 ID", example = "1")
            @PathVariable("memberId") Long memberId
    ) {
        memberService.deleteMember(memberId);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "회원을 삭제했습니다.", null));
    }

    @Operation(summary = "회원 정보 상세 조회", description = "회원 정보를 상세 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 정보 상세 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(변환 불가능한 문자열)"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "잘못된 요청(회원 정보 없음)"),
    })
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResult<MemberResponseDto>> getMemberDetail(
            @Parameter(description = "회원 ID", example = "1")
            @PathVariable("memberId") Long memberId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        boolean isManager = Role.MANAGER.getValue().equals(member.getRole());

        MemberResponseDto memberResponseDto = isManager
                ? memberService.getMemberByIdForManager(memberId)
                : memberService.getMemberById(memberId);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "회원 정보를 조회했습니다.", memberResponseDto));
    }
}