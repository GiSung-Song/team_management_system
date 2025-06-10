package com.manager.taskmanager.global.batch.notification;

import com.manager.taskmanager.global.error.CustomException;
import com.manager.taskmanager.global.error.ErrorCode;
import com.manager.taskmanager.global.log.annotation.SaveLogging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class DeleteNotificationScheduler {

    private final JobLauncher jobLauncher;
    private final Job deleteNotificationsJob;

    @Scheduled(cron = "0 0 3 * * *")
    @SaveLogging(eventName = "배치 - 오래된 알림 삭제")
    public void run() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        try {
            jobLauncher.run(deleteNotificationsJob, jobParameters);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.ERROR_DELETE_NOTIFICATION_BATCH);
        }
    }
}