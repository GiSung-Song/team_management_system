package com.manager.taskmanager.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 목록 선택 Request DTO")
public class NotificationIdListDto {

    @Valid
    @NotEmpty(message = "최소 하나의 알림을 선택해주세요.")
    List<NotificationIdDto> notificationIdList = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "알림 선택 ID Request DTO")
    public static class NotificationIdDto {

        @Schema(description = "알림 ID")
        @NotNull(message = "알림 ID는 필수 입력 값 입니다.")
        private Long notificationId;
    }

}
