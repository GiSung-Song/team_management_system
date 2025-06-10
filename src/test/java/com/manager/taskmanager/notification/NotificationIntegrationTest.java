package com.manager.taskmanager.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.taskmanager.config.DBContainerExtension;
import com.manager.taskmanager.config.TestDataFactory;
import com.manager.taskmanager.department.entity.Department;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.member.entity.Role;
import com.manager.taskmanager.notification.dto.NotificationIdListDto;
import com.manager.taskmanager.notification.entity.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@ExtendWith(DBContainerExtension.class)
@AutoConfigureMockMvc
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    private Department department;
    private Member member;

    private Notification notification1;
    private Notification notification2;
    private Notification notification3;
    private Notification notification4;

    @BeforeEach
    void setUp() {
        department = testDataFactory.createDepartment();
        member = testDataFactory.createMember(department);

        LocalDate today = LocalDate.now();

        notification1 = testDataFactory.createNotification(member, "메시지1", today.minusDays(10));
        notification2 = testDataFactory.createNotification(member, "메시지2", today.minusDays(11));
        notification3 = testDataFactory.createNotification(member, "메시지3", today.minusDays(12));
        notification4 = testDataFactory.createNotification(member, "메시지4", today.minusDays(13));
    }

    @Nested
    @DisplayName("알림 목록 조회 API")
    class 알림_목록_조회_API_테스트 {

        @Test
        @DisplayName("알림 목록 조회 정상")
        void whenValidLogin_thenReturnNotificationList() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            mockMvc.perform(get("/api/notifications"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.notificationList[*].message", containsInAnyOrder("메시지1", "메시지2", "메시지3", "메시지4")));
        }

        @Test
        @DisplayName("비 로그인 시 401 반환")
        void whenUnauthenticated_thenReturnUnAuthorized() throws Exception {
            mockMvc.perform(get("/api/notifications"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("알림 단건 읽음 처리 API")
    class 알림_단건_읽음_처리_API_테스트 {

        @Test
        @DisplayName("알림 단건 읽음 처리 정상")
        void whenValidInput_thenNotificationIsRead() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            mockMvc.perform(patch("/api/notifications/{notificationId}", notification1.getId()))
                    .andExpect(status().isOk())
                    .andDo(print());

            Notification notification = notificationRepository.findById(notification1.getId())
                    .orElseThrow();

            assertThat(notification.isRead()).isTrue();
        }

        @Test
        @DisplayName("비 로그인 시 401 반환")
        void whenUnauthenticated_thenReturnUnAuthorized() throws Exception {
            mockMvc.perform(patch("/api/notifications/{notificationId}", notification1.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("해당 알림 번호 없을 시 404 반환")
        void whenNotFoundNotification_thenReturnNotFound() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            mockMvc.perform(patch("/api/notifications/{notificationId}", 342L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("알림 다건 읽음 처리 API")
    class 알림_다건_읽음_처리_API_테스트 {

        @Test
        @DisplayName("알림 다건 읽음 처리 정상")
        void whenValidInput_thenNotificationsIsRead() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            List<Notification> unReadNotifications = List.of(notification1, notification2, notification3);
            List<Long> unReadIds = unReadNotifications.stream()
                    .map(Notification::getId)
                    .toList();

            NotificationIdListDto notificationIdListDto = new NotificationIdListDto(
                    unReadIds.stream()
                            .map(NotificationIdListDto.NotificationIdDto::new)
                            .toList()
            );

            mockMvc.perform(patch("/api/notifications/read")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(notificationIdListDto)))
                    .andExpect(status().isOk())
                    .andDo(print());

            for (Notification notification : unReadNotifications) {
                Notification updateNotification = notificationRepository.findById(notification.getId()).orElseThrow();
                assertThat(updateNotification.isRead()).isTrue();
            }

            assertThat(notification4.isRead()).isFalse();
        }

        @Test
        @DisplayName("비 로그인 시 401 반환")
        void whenUnauthenticated_thenReturnUnAuthorized() throws Exception {
            mockMvc.perform(patch("/api/notifications/read"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("해당 알림 번호 없을 시 404 반환")
        void whenNotFoundNotification_thenReturnNotFound() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            List<NotificationIdListDto.NotificationIdDto> notificationIdDtos =
                    List.of(
                            new NotificationIdListDto.NotificationIdDto(notification1.getId()),
                            new NotificationIdListDto.NotificationIdDto(notification2.getId()),
                            new NotificationIdListDto.NotificationIdDto(432143214321L)
                    );

            NotificationIdListDto notificationListDto = new NotificationIdListDto(notificationIdDtos);

            mockMvc.perform(patch("/api/notifications/read")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(notificationListDto)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("알림 단건 삭제 API")
    class 알림_단건_삭제_API_테스트 {

        @Test
        @DisplayName("알림 단건 삭제 정상")
        void whenValidInput_thenNotificationIsDeleted() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            mockMvc.perform(delete("/api/notifications/{notificationId}", notification1.getId()))
                    .andExpect(status().isOk())
                    .andDo(print());

            Notification notification = notificationRepository.findById(notification1.getId())
                    .orElse(null);

            assertThat(notification).isNull();
        }

        @Test
        @DisplayName("비 로그인 시 401 반환")
        void whenUnauthenticated_thenReturnUnAuthorized() throws Exception {
            mockMvc.perform(delete("/api/notifications/{notificationId}", notification1.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("해당 알림 번호 없을 시 404 반환")
        void whenNotFoundNotification_thenReturnNotFound() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            mockMvc.perform(delete("/api/notifications/{notificationId}", 342L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("알림 다건 삭제 API")
    class 알림_다건_삭제_API_테스트 {

        @Test
        @DisplayName("알림 다건 삭제 정상")
        void whenValidInput_thenNotificationsIsDeleted() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            List<Notification> deleteNotification = List.of(notification1, notification2, notification3);
            List<Long> deleteNotificationIds = deleteNotification.stream()
                    .map(Notification::getId)
                    .toList();

            NotificationIdListDto notificationIdListDto = new NotificationIdListDto(
                    deleteNotificationIds.stream()
                            .map(NotificationIdListDto.NotificationIdDto::new)
                            .toList()
            );

            mockMvc.perform(delete("/api/notifications/delete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(notificationIdListDto)))
                    .andExpect(status().isOk())
                    .andDo(print());

            List<Notification> allNotification = notificationRepository.findAll();

            assertThat(allNotification.size()).isEqualTo(1);
            assertThat(allNotification.get(0).getId()).isEqualTo(notification4.getId());
        }

        @Test
        @DisplayName("비 로그인 시 401 반환")
        void whenUnauthenticated_thenReturnUnAuthorized() throws Exception {
            mockMvc.perform(delete("/api/notifications/delete"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("해당 알림 번호 없을 시 404 반환")
        void whenNotFoundNotification_thenReturnNotFound() throws Exception {
            testDataFactory.setAuthentication(member, Role.MEMBER);

            List<NotificationIdListDto.NotificationIdDto> notificationIdDtos =
                    List.of(
                            new NotificationIdListDto.NotificationIdDto(notification1.getId()),
                            new NotificationIdListDto.NotificationIdDto(notification2.getId()),
                            new NotificationIdListDto.NotificationIdDto(432143214321L)
                    );

            NotificationIdListDto notificationListDto = new NotificationIdListDto(notificationIdDtos);

            mockMvc.perform(delete("/api/notifications/delete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(notificationListDto)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
}