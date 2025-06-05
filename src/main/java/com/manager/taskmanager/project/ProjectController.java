package com.manager.taskmanager.project;

import com.manager.taskmanager.common.ApiResult;
import com.manager.taskmanager.config.security.CustomUserDetails;
import com.manager.taskmanager.project.dto.*;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "프로젝트 관련 API")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "프로젝트 생성", description = "프로젝트를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "프로젝트 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효성 검사 실패)"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(직급이 낮은 경우)")
    })
    @PostMapping
    public ResponseEntity<ApiResult<Void>> createProject(
            @Valid @RequestBody
            ProjectRegisterDto projectRegisterDto,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        projectService.createProject(member.getId(), projectRegisterDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(HttpStatus.CREATED, "프로젝트를 생성했습니다.", null));
    }

    @Operation(summary = "프로젝트 수정", description = "프로젝트를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로젝트 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효성 검사 실패)"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(직급/소속)")
    })
    @PatchMapping("/{projectId}")
    public ResponseEntity<ApiResult<Void>> createProject(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable("projectId") Long projectId,

            @Valid @RequestBody
            ProjectUpdateDto projectUpdateDto,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        projectService.updateProject(member.getId(), projectId, projectUpdateDto);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "프로젝트를 수정했습니다.", null));
    }

    @Operation(summary = "프로젝트 목록 조회", description = "프로젝트 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로젝트 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자")
    })
    @GetMapping
    public ResponseEntity<ApiResult<ProjectListDto>> getProjectList(
            @Parameter(description = "프로젝트명, 회원이름, 프로젝트 진행 상태 필터")
            @ModelAttribute ProjectSearchCondition condition) {
        ProjectListDto projectList = projectService.getProjectList(condition);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "프로젝트 목록을 조회했습니다.", projectList));
    }

    @Operation(summary = "프로젝트 상세 조회", description = "프로젝트를 상세 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로젝트 상세 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResult<ProjectDetailDto>> getProjectDetail(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable("projectId") Long projectId
    ) {
        ProjectDetailDto projectDetail = projectService.getProjectDetail(projectId);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "프로젝트를 상세 조회했습니다.", projectDetail));
    }

    @Operation(summary = "프로젝트 삭제", description = "프로젝트를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로젝트 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(직급/소속)")
    })
    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResult<Void>> deleteProject(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable("projectId") Long projectId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        projectService.deleteProject(member.getId(), projectId);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "프로젝트를 삭제했습니다.", null));
    }
}
