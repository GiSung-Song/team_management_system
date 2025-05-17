package com.manager.taskmanager.department;

import com.manager.taskmanager.common.ApiResult;
import com.manager.taskmanager.department.dto.AllDepartmentListDto;
import com.manager.taskmanager.department.dto.DepartmentDto;
import com.manager.taskmanager.department.dto.DepartmentRegisterDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/department")
@Tag(name = "Department",description = "부서 관련 API")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "부서 전체 목록 조회", description = "모든 부서 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "부서 목록 조회 성공")
    })
    @GetMapping
    public ResponseEntity<ApiResult<AllDepartmentListDto>> getAllDepartments() {
        AllDepartmentListDto allDepartment = departmentService.getAllDepartment();

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "모든 부서를 조회했습니다.", allDepartment));
    }

    @Operation(summary = "부서 등록", description = "부서를 새로 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "부서 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효성 검사 실패, 이미 등록된 부서)"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(관리자 전용)")
    })
    @PostMapping
    public ResponseEntity<ApiResult<Void>> registerDepartment(@RequestBody @Valid DepartmentRegisterDto dto) {
        departmentService.registerDepartment(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResult.success(HttpStatus.CREATED, "부서를 생성했습니다.", null));
    }

    @Operation(summary = "부서 삭제", description = "부서를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "부서 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(변환 불가능한 문자열)"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음(관리자 전용)")
    })
    @DeleteMapping("/{departmentId}")
    public ResponseEntity<ApiResult<Void>> deleteDepartment(
            @Parameter(description = "부서 ID", example = "1")
            @PathVariable("departmentId") Long departmentId
    ) {
        departmentService.deleteDepartment(departmentId);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "부서를 삭제했습니다.", null));
    }
}
