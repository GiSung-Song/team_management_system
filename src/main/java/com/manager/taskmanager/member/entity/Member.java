package com.manager.taskmanager.member.entity;

import com.manager.taskmanager.common.BaseTimeEntity;
import com.manager.taskmanager.department.entity.Department;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "members")
@SQLDelete(sql = "UPDATE members SET deleted_at = NOW() WHERE id = ?")
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String employeeNumber;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 10)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 20, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    private LocalDateTime deletedAt;

    public static Member createMember(String employeeNumber, String password, String name, String email,
                                String phoneNumber, Position position, Department department) {
        return Member.builder()
                .employeeNumber(employeeNumber)
                .password(password)
                .name(name)
                .role(Role.MEMBER)
                .email(email)
                .phoneNumber(phoneNumber)
                .position(position)
                .department(department)
                .build();
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updatePosition(Position position) {
        this.position = position;
    }

    public void updateDepartment(Department department) {
        this.department = department;
    }
}
