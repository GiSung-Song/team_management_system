package com.manager.taskmanager.global.batch.notification;

import com.manager.taskmanager.notification.NotificationRepository;
import com.manager.taskmanager.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SaveNotificationWriter implements ItemWriter<Notification> {

    private final NotificationRepository notificationRepository;

    @Override
    public void write(Chunk<? extends Notification> chunk) throws Exception {
        List<Notification> notifications = new ArrayList<>(chunk.getItems());

        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }
}