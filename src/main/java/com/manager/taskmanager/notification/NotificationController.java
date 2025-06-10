package com.manager.taskmanager.notification;

import com.manager.taskmanager.global.config.security.CustomUserDetails;
import com.manager.taskmanager.global.response.ApiResult;
import com.manager.taskmanager.notification.dto.NotificationDto;
import com.manager.taskmanager.notification.dto.NotificationIdListDto;
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
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 목록 조회
    @Operation(summary = "알림 목록 조회", description = "알림 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자")
    })
    @GetMapping
    public ResponseEntity<ApiResult<NotificationDto>> getNotificationList(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        NotificationDto notificationList = notificationService.getNotificationList(member.getId());

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "알림 목록을 조회했습니다.", notificationList));
    }

    // 알림 단건 읽음 처리
    @Operation(summary = "알림 단건 읽음 처리", description = "알림 단건을 읽음 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 단건 읽음 처리 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 알림")
    })
    @PatchMapping("/{notificationId}")
    public ResponseEntity<ApiResult<Void>> readNotification(
            @Parameter(description = "알림 ID", example = "1")
            @PathVariable("notificationId") Long notificationId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        notificationService.readNotification(member.getId(), notificationId);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "알림 단건을 읽음 처리했습니다.", null));
    }

    // 알림 다건 읽음 처리
    @Operation(summary = "알림 읽음 다건 처리", description = "알림 다건을 읽음 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 다건 읽음 처리 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 알림")
    })
    @PatchMapping("/read")
    public ResponseEntity<ApiResult<Void>> readNotifications(
            @Valid @RequestBody
            NotificationIdListDto notificationDto,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        notificationService.readNotifications(member.getId(), notificationDto);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "알림 다건을 읽음 처리했습니다.", null));
    }

    // 알림 단건 삭제
    @Operation(summary = "알림 단건 삭제", description = "알림 단건을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 단건 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 알림")
    })
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResult<Void>> deleteNotification(
            @Parameter(description = "알림 ID", example = "1")
            @PathVariable("notificationId") Long notificationId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        notificationService.deleteNotification(member.getId(), notificationId);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "알림 단건을 삭제했습니다.", null));
    }

    // 알림 다건 삭제
    @Operation(summary = "알림 다건 삭제", description = "알림 다건을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 다건 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 알림")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResult<Void>> deleteNotifications(
            @Valid @RequestBody
            NotificationIdListDto notificationDto,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        notificationService.deleteNotifications(member.getId(), notificationDto);

        return ResponseEntity.ok(
                ApiResult.success(HttpStatus.OK, "알림 다건을 삭제했습니다.", null));
    }
}
