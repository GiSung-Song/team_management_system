package com.manager.taskmanager.notification.entity;

import com.manager.taskmanager.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "notifications",
    uniqueConstraints = @UniqueConstraint(
            columnNames = {"member_id", "created_date"}
    )
)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private LocalDate createdDate = LocalDate.now();

    public static Notification createNotification(Member member, String message) {
        Notification notification = Notification.builder()
                .member(member)
                .message(message)
                .isRead(false)
                .createdDate(LocalDate.now())
                .build();

        return notification;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
