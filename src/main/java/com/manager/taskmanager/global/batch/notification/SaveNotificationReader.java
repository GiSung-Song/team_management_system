package com.manager.taskmanager.global.batch.notification;

import com.manager.taskmanager.member.MemberQueryRepository;
import com.manager.taskmanager.member.dto.MemberTaskCountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SaveNotificationReader implements ItemReader<MemberTaskCountDto> {

    private final MemberQueryRepository memberQueryRepository;
    private Iterator<MemberTaskCountDto> idIterator;

    @Override
    public MemberTaskCountDto read() throws Exception {
        if (idIterator == null) {
            LocalDate today = LocalDate.now();

            List<MemberTaskCountDto> memberIds = memberQueryRepository.getRemainingTaskCountByMember(today);
            idIterator = memberIds.iterator();
        }

        return idIterator.hasNext() ? idIterator.next() : null;
    }
}
