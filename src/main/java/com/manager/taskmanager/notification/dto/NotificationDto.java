package com.manager.taskmanager.notification.dto;

import com.manager.taskmanager.notification.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 목록 Response DTO")
public class NotificationDto {

    private List<NotificationInfo> notificationList;

    public static NotificationDto of(List<Notification> notificationList) {
        List<NotificationInfo> notificationInfoList = notificationList.stream()
                .map(NotificationInfo::of)
                .collect(Collectors.toList());

        return new NotificationDto(notificationInfoList);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "알림 정보 Response DTO")
    public static class NotificationInfo {

        @Schema(description = "알림 ID")
        private Long notificationId;

        @Schema(description = "알림 내용")
        private String message;

        @Schema(description = "알림 날짜")
        private LocalDate notificationDate;

        @Schema(description = "읽음 여부")
        private boolean isRead;

        public static NotificationInfo of(Notification notification) {
            return new NotificationInfo(
                    notification.getId(),
                    notification.getMessage(),
                    notification.getCreatedDate(),
                    notification.isRead()
            );
        }
    }
}
