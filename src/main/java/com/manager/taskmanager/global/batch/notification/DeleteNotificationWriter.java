package com.manager.taskmanager.global.batch.notification;

import com.manager.taskmanager.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeleteNotificationWriter implements ItemWriter<Long> {

    private final NotificationRepository notificationRepository;

    @Override
    public void write(Chunk<? extends Long> chunk) throws Exception {
        List<Long> notificationIdList = new ArrayList<>(chunk.getItems());

        if (!notificationIdList.isEmpty()) {
            notificationRepository.deleteAllByIdInBatch(notificationIdList);
        }
    }
}