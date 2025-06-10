package com.manager.taskmanager.global.batch.notification;

import com.manager.taskmanager.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
@Component
public class DeleteNotificationReader implements ItemReader<Long> {

    private final NotificationRepository notificationRepository;
    private Iterator<Long> idIterator;

    @Override
    public Long read() throws Exception {
        if (idIterator == null) {
            LocalDate date = LocalDate.now().minusMonths(1);

            List<Long> taskIds = notificationRepository.findOldReadNotifications(date);
            idIterator = taskIds.iterator();
        }

        return idIterator.hasNext() ? idIterator.next() : null;
    }
}
