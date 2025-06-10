package com.manager.taskmanager.notification;

import com.manager.taskmanager.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByMemberIdOrderByCreatedDateDesc(Long memberId);
    Notification findByIdAndMemberId(Long notificationId, Long memberId);
    List<Notification> findAllByIdInAndMemberId(List<Long> notificationIdList, Long memberId);

    @Query("select n.id from Notification n where n.isRead = true and n.createdDate < :date")
    List<Long> findOldReadNotifications(@Param("date")LocalDate date);
}