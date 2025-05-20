package com.manager.taskmanager.member;

import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.member.entity.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
public class MemberQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public MemberQueryRepository(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<Member> getAllMemberList(String departmentName, String name, boolean isManager) {
        QMember member = QMember.member;

        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(departmentName)) {
            builder.and(member.department.departmentName.containsIgnoreCase(departmentName));
        }

        if (StringUtils.hasText(name)) {
            builder.and(member.name.containsIgnoreCase(name));
        }

        if (!isManager) {
            builder.and(member.deletedAt.isNull());
        }

        return jpaQueryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }
}
