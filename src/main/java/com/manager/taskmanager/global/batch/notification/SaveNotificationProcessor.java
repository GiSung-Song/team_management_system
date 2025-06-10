package com.manager.taskmanager.global.batch.notification;

import com.manager.taskmanager.global.error.CustomException;
import com.manager.taskmanager.global.error.ErrorCode;
import com.manager.taskmanager.member.MemberRepository;
import com.manager.taskmanager.member.dto.MemberTaskCountDto;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class SaveNotificationProcessor implements ItemProcessor<MemberTaskCountDto, Notification> {

    private final MemberRepository memberRepository;

    @Override
    public Notification process(MemberTaskCountDto memberTaskCountDto) throws Exception {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberTaskCountDto.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd (E)", Locale.KOREA);
        LocalDate today = LocalDate.now();
        String date = today.format(dateTimeFormatter);

        String message = String.format("%s - 오늘 마감해야 할 업무가 %d건 있습니다.", date, memberTaskCountDto.getTaskCount());

        return Notification.createNotification(member, message);
    }
}
