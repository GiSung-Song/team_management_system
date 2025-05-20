package com.manager.taskmanager.member;

import com.manager.taskmanager.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByIdAndDeletedAtIsNull(Long memberId);
    boolean existsByEmployeeNumber(String employeeNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
}