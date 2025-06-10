package com.manager.taskmanager.notification;

import com.manager.taskmanager.config.DBContainerExtension;
import com.manager.taskmanager.config.TestDataFactory;
import com.manager.taskmanager.department.entity.Department;
import com.manager.taskmanager.global.error.CustomException;
import com.manager.taskmanager.global.error.ErrorCode;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.notification.dto.NotificationDto;
import com.manager.taskmanager.notification.dto.NotificationIdListDto;
import com.manager.taskmanager.notification.entity.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@ExtendWith(DBContainerExtension.class)
class NotificationServiceTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    private Department department;
    private Member leader;
    private Member member;
    private Member manager;

    @BeforeEach
    void setUp() {
        department = testDataFactory.createDepartment();
        leader = testDataFactory.createLeader(department);
        member = testDataFactory.createMember(department);
        manager = testDataFactory.createManager(department);
    }

    @Nested
    @DisplayName("알림 목록 조회 Service")
    class 알림_목록_조회_서비스_테스트 {

        @Test
        @DisplayName("알림 목록 조회 정상")
        void whenValidLogin_thenReturnNotificationList() {
            LocalDate today = LocalDate.now();

            testDataFactory.createNotification(member, "메시지1", today.minusDays(10));
            testDataFactory.createNotification(member, "메시지2", today.minusDays(11));
            testDataFactory.createNotification(member, "메시지3", today.minusDays(12));
            testDataFactory.createNotification(member, "메시지4", today.minusDays(13));

            NotificationDto notificationDto = notificationService.getNotificationList(member.getId());

            assertThat(notificationDto.getNotificationList())
                    .hasSize(4)
                    .extracting("message")
                    .containsExactlyInAnyOrder("메시지1", "메시지2", "메시지3", "메시지4");
        }
    }

    @Nested
    @DisplayName("알림 단건 읽음 처리 Service")
    class 알림_단건_읽음_처리_서비스_테스트 {

        @Test
        @DisplayName("알림 단건 읽음 처리 정상")
        void whenValidInput_thenNotificationIsRead() {
            LocalDate today = LocalDate.now();

            Notification notification = testDataFactory.createNotification(member, "메시지1", today.minusDays(10));

            notificationService.readNotification(member.getId(), notification.getId());

            Notification findNotification = notificationRepository.findById(notification.getId())
                    .orElseThrow();

            assertThat(findNotification.getMessage()).isEqualTo(notification.getMessage());
            assertThat(findNotification.isRead()).isTrue();
        }

        @Test
        @DisplayName("알림 없을 시 404 반환")
        void whenInvalidNotificationId_thenReturnNotFound() {
            LocalDate today = LocalDate.now();
            Notification notification = testDataFactory.createNotification(member, "메시지1", today.minusDays(10));

            assertThatThrownBy(() -> notificationService.readNotification(member.getId(), 12312L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("알림 다건 읽음 처리 Service")
    class 알림_다건_읽음_처리_서비스_테스트 {

        @Test
        @DisplayName("알림 다건 읽음 처리 정상")
        void whenValidInput_thenNotificationsAreRead() {
            LocalDate today = LocalDate.now();
            Notification notification1 = testDataFactory.createNotification(member, "메시지1", today.minusDays(10));
            Notification notification2 = testDataFactory.createNotification(member, "메시지2", today.minusDays(11));
            Notification notification3 = testDataFactory.createNotification(member, "메시지3", today.minusDays(12));
            Notification notification4 = testDataFactory.createNotification(member, "메시지4", today.minusDays(13));

            List<Notification> unReadNotifications = List.of(notification1, notification2, notification3);
            List<Long> unReadIds = unReadNotifications.stream()
                    .map(Notification::getId)
                    .toList();

            NotificationIdListDto notificationIdListDto = new NotificationIdListDto(
                    unReadIds.stream()
                            .map(NotificationIdListDto.NotificationIdDto::new)
                            .toList()
            );

            notificationService.readNotifications(member.getId(), notificationIdListDto);

            for (Notification notification : unReadNotifications) {
                Notification updateNotification = notificationRepository.findById(notification.getId()).orElseThrow();
                assertThat(updateNotification.isRead()).isTrue();
            }

            assertThat(notification4.isRead()).isFalse();
        }

        @Test
        @DisplayName("알림 없을 시 404 반환")
        void whenInvalidNotificationId_thenReturnNotFound() {
            LocalDate today = LocalDate.now();
            Notification notification1 = testDataFactory.createNotification(member, "메시지1", today.minusDays(10));
            Notification notification2 = testDataFactory.createNotification(member, "메시지2", today.minusDays(11));
            Notification notification3 = testDataFactory.createNotification(member, "메시지3", today.minusDays(12));

            List<NotificationIdListDto.NotificationIdDto> notificationIdDtos =
                    List.of(
                            new NotificationIdListDto.NotificationIdDto(notification1.getId()),
                            new NotificationIdListDto.NotificationIdDto(notification2.getId()),
                            new NotificationIdListDto.NotificationIdDto(432143214321L)
                    );

            NotificationIdListDto notificationListDto = new NotificationIdListDto(notificationIdDtos);

            assertThatThrownBy(() -> notificationService.readNotifications(member.getId(), notificationListDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("알림 단건 삭제 Service")
    class 알림_단건_삭제_서비스_테스트 {

        @Test
        @DisplayName("알림 단건 삭제 정상")
        void whenValidInput_thenNotificationIsDeleted() {
            LocalDate today = LocalDate.now();
            Notification notification = testDataFactory.createNotification(member, "메시지1", today.minusDays(10));

            notificationService.deleteNotification(member.getId(), notification.getId());

            Notification findNotification = notificationRepository.findById(notification.getId())
                    .orElse(null);

            assertThat(findNotification).isNull();
        }

        @Test
        @DisplayName("알림 없을 시 404 반환")
        void whenInvalidNotificationId_thenReturnNotFound() {
            LocalDate today = LocalDate.now();
            Notification notification = testDataFactory.createNotification(member, "메시지1", today.minusDays(10));

            assertThatThrownBy(() -> notificationService.deleteNotification(member.getId(), 12312L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("알림 다건 삭제 Service")
    class 알림_다건_삭제_서비스_테스트 {

        @Test
        @DisplayName("알림 다건 삭제 정상")
        void whenValidInput_thenNotificationsAreDeleted() {
            LocalDate today = LocalDate.now();
            Notification notification1 = testDataFactory.createNotification(member, "메시지1", today.minusDays(10));
            Notification notification2 = testDataFactory.createNotification(member, "메시지2", today.minusDays(11));
            Notification notification3 = testDataFactory.createNotification(member, "메시지3", today.minusDays(12));

            List<Notification> deleteNotificationList = List.of(notification1, notification2, notification3);
            List<Long> deleteNotificationIds = deleteNotificationList.stream()
                    .map(Notification::getId)
                    .toList();

            NotificationIdListDto notificationIdListDto = new NotificationIdListDto(
                    deleteNotificationIds.stream()
                            .map(NotificationIdListDto.NotificationIdDto::new)
                            .toList()
            );

            notificationService.deleteNotifications(member.getId(), notificationIdListDto);

            List<Notification> allNotification = notificationRepository.findAll();

            assertThat(allNotification).isEmpty();
        }

        @Test
        @DisplayName("알림 없을 시 404 반환")
        void whenInvalidNotificationId_thenReturnNotFound() {
            LocalDate today = LocalDate.now();
            Notification notification1 = testDataFactory.createNotification(member, "메시지1", today.minusDays(10));
            Notification notification2 = testDataFactory.createNotification(member, "메시지2", today.minusDays(11));
            Notification notification3 = testDataFactory.createNotification(member, "메시지3", today.minusDays(12));

            List<NotificationIdListDto.NotificationIdDto> notificationIdDtos =
                    List.of(
                            new NotificationIdListDto.NotificationIdDto(notification1.getId()),
                            new NotificationIdListDto.NotificationIdDto(notification2.getId()),
                            new NotificationIdListDto.NotificationIdDto(432143214321L)
                    );

            NotificationIdListDto notificationListDto = new NotificationIdListDto(notificationIdDtos);

            assertThatThrownBy(() -> notificationService.readNotifications(member.getId(), notificationListDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }
}