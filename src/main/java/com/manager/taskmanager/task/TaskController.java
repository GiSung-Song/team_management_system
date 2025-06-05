package com.manager.taskmanager.task;

import com.manager.taskmanager.common.ApiResult;
import com.manager.taskmanager.config.security.CustomUserDetails;
import com.manager.taskmanager.member.entity.Role;
import com.manager.taskmanager.task.dto.*;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Task", description = "업무 관련 API")
public class TaskController {

    private final TaskService taskService;

    // 업무 등록
    @Operation(summary = "업무 등록", description = "업무를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "업무 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효성 검사 실패)"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "프로젝트에 속하지 않은 사용자")
    })
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<ApiResult<Void>> addTask(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable("projectId") Long projectId,

            @Valid @RequestBody
            AddTaskDto addTaskDto,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        taskService.addTask(member.getId(), projectId, addTaskDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(HttpStatus.CREATED, "업무를 등록했습니다.", null));
    }

    // 업무 수정
    @Operation(summary = "업무 수정", description = "업무를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업무 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효성 검사 실패)"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(자신의 업무가 아님)"),
            @ApiResponse(responseCode = "404", description = "프로젝트에 속하지 않은 사용자")
    })
    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResult<Void>> updateTask(
            @Parameter(description = "업무 ID", example = "1")
            @PathVariable("taskId") Long taskId,

            @Valid @RequestBody
            UpdateTaskDto updateTaskDto,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        taskService.updateTask(member.getId(), taskId, updateTaskDto);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "업무를 수정했습니다.", null));
    }

    // 업무 목록 조회
    @Operation(summary = "업무 목록 조회", description = "업무 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업무 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
    })
    @GetMapping("/tasks")
    public ResponseEntity<ApiResult<TaskListDto>> getTaskDetail(
            @Parameter(description = "프로젝트명, 업무명, 업무 진행 상태 필터")
            @ModelAttribute TaskSearchCondition condition,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        boolean isManager = Role.from(member.getRole()) == Role.MANAGER;

        TaskListDto taskList = taskService.getTaskList(member.getId(), isManager, condition);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "업무 목록을 조회했습니다.", taskList));
    }

    // 업무 상세 조회
    @Operation(summary = "업무 상세 조회", description = "업무를 상세 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업무 상세 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효성 검사 실패)"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(자신의 업무가 아님)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 업무")
    })
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResult<TaskDetailDto>> getTaskList(
            @Parameter(description = "업무 ID", example = "1")
            @PathVariable("taskId") Long taskId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        boolean isManager = Role.from(member.getRole()) == Role.MANAGER;

        TaskDetailDto taskDetail = taskService.getTaskDetail(member.getId(), taskId, isManager);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "업무를 조회했습니다.", taskDetail));
    }

    // 업무 삭제
    @Operation(summary = "업무 삭제", description = "업무를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업무 상세 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효성 검사 실패)"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(자신의 업무가 아님)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 업무")
    })
    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResult<Void>> deleteTask(
            @Parameter(description = "업무 ID", example = "1")
            @PathVariable("taskId") Long taskId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        taskService.deleteTask(member.getId(), taskId);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "업무를 삭제했습니다.", null));
    }
}