package com.manager.taskmanager.member.entity;

import com.manager.taskmanager.common.BaseTimeEntity;
import com.manager.taskmanager.department.entity.Department;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "members")
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
    private MemberStatus memberStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    public static Member createMember(String employeeNumber, String password, String name, String email,
                                String phoneNumber, Position position, Department department) {
        return Member.builder()
                .employeeNumber(employeeNumber)
                .password(password)
                .name(name)
                .role(Role.MEMBER)
                .email(email)
                .phoneNumber(phoneNumber)
                .memberStatus(MemberStatus.ACTIVE)
                .position(position)
                .department(department)
                .build();
    }
}
