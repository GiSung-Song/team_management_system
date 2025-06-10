package com.manager.taskmanager.notification;

import com.manager.taskmanager.global.error.CustomException;
import com.manager.taskmanager.global.error.ErrorCode;
import com.manager.taskmanager.notification.dto.NotificationDto;
import com.manager.taskmanager.notification.dto.NotificationIdListDto;
import com.manager.taskmanager.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // 알림 목록 조회
    @Transactional(readOnly = true)
    public NotificationDto getNotificationList(Long memberId) {
        List<Notification> notificationList = notificationRepository.findAllByMemberIdOrderByCreatedDateDesc(memberId);

        return NotificationDto.of(notificationList);
    }

    // 알림 단건 읽음 처리
    @Transactional
    public void readNotification(Long memberId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndMemberId(notificationId, memberId);

        if (notification == null) {
            throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        if (!notification.isRead()) {
            notification.markAsRead();
        }
    }

    // 알림 다건 읽음 처리
    @Transactional
    public void readNotifications(Long memberId, NotificationIdListDto dto) {
        List<Long> notificationIdList = dto.getNotificationIdList().stream()
                .map(m -> m.getNotificationId())
                .collect(Collectors.toList());

        List<Notification> notificationList = notificationRepository.findAllByIdInAndMemberId(notificationIdList, memberId);

        if (notificationIdList.size() != notificationList.size()) {
            throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        for (Notification notification : notificationList) {
            if (!notification.isRead()) {
                notification.markAsRead();
            }
        }
    }

    // 알림 단건 삭제
    @Transactional
    public void deleteNotification(Long memberId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndMemberId(notificationId, memberId);

        if (notification == null) {
            throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notificationRepository.delete(notification);
    }

    // 알림 다건 삭제
    @Transactional
    public void deleteNotifications(Long memberId, NotificationIdListDto dto) {
        List<Long> notificationIdList = dto.getNotificationIdList().stream()
                .map(m -> m.getNotificationId())
                .collect(Collectors.toList());

        List<Notification> notificationList = notificationRepository.findAllByIdInAndMemberId(notificationIdList, memberId);

        if (notificationIdList.size() != notificationList.size()) {
            throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notificationRepository.deleteAll(notificationList);
    }

}
