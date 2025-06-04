package com.manager.taskmanager.projectmember;

import com.manager.taskmanager.common.ApiResult;
import com.manager.taskmanager.config.security.CustomUserDetails;
import com.manager.taskmanager.projectmember.dto.ProjectMemberRegisterDto;
import com.manager.taskmanager.projectmember.dto.ProjectMemberUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
@Tag(name = "Project Member", description = "프로젝트 멤버 관련 API")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    // 추가
    @Operation(summary = "프로젝트 멤버 추가", description = "프로젝트 멤버를 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "프로젝트 멤버 추가 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효성 검사 실패)"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(직급/권한)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    @PostMapping("/{projectId}/member")
    public ResponseEntity<ApiResult<Void>> addProjectMember(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable("projectId") Long projectId,

            @Valid @RequestBody
            ProjectMemberRegisterDto projectMemberRegisterDto,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        projectMemberService.addProjectMember(member.getId(), projectId, projectMemberRegisterDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(HttpStatus.CREATED, "프로젝트 멤버를 추가했습니다.", null));
    }

    // 수정
    @Operation(summary = "프로젝트 멤버 정보 수정", description = "프로젝트 멤버 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로젝트 멤버 정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효성 검사 실패)"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(직급이 낮은 경우)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    @PatchMapping("/{projectId}/member/{memberId}")
    public ResponseEntity<ApiResult<Void>> updateProjectMember(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable("projectId") Long projectId,

            @Parameter(description = "회원 ID", example = "1")
            @PathVariable("memberId") Long memberId,

            @Valid @RequestBody
            ProjectMemberUpdateDto projectMemberUpdateDto,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        projectMemberService.updateProjectMember(member.getId(), projectId, memberId, projectMemberUpdateDto);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "프로젝트 멤버 정보를 수정했습니다.", null));
    }

    // 삭제
    @Operation(summary = "프로젝트 멤버 삭제", description = "프로젝트 멤버를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로젝트 멤버 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(직급이 낮은 경우)")
    })
    @DeleteMapping("/{projectId}/member/{memberId}")
    public ResponseEntity<ApiResult<Void>> deleteProjectMember(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable("projectId") Long projectId,

            @Parameter(description = "회원 ID", example = "1")
            @PathVariable("memberId") Long memberId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        projectMemberService.deleteProjectMember(member.getId(), projectId, memberId);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "프로젝트 멤버를 삭제했습니다.", null));
    }
}
