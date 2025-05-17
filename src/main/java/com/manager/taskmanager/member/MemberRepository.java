package com.manager.taskmanager.member;

import com.manager.taskmanager.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
