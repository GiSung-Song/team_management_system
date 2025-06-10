package com.manager.taskmanager.global.batch.notification;

import com.manager.taskmanager.member.dto.MemberTaskCountDto;
import com.manager.taskmanager.notification.entity.Notification;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@EnableBatchProcessing
@Configuration
public class SaveNotificationBatchConfig {

    @Bean
    public Step saveNotificationsStep(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            SaveNotificationReader saveNotificationReader,
            SaveNotificationProcessor saveNotificationProcessor,
            SaveNotificationWriter saveNotificationWriter) {

        return new StepBuilder("saveNotificationsStep", jobRepository)
                .<MemberTaskCountDto, Notification>chunk(100, platformTransactionManager)
                .reader(saveNotificationReader)
                .processor(saveNotificationProcessor)
                .writer(saveNotificationWriter)
                .build();
    }

    @Bean
    public Job saveNotificationsJob(JobRepository jobRepository, Step saveNotificationsStep) {
        return new JobBuilder("saveNotificationsJob", jobRepository)
                .start(saveNotificationsStep)
                .build();

    }
}
