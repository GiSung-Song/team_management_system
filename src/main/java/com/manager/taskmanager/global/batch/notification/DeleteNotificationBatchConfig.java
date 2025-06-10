package com.manager.taskmanager.global.batch.notification;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class DeleteNotificationBatchConfig {

    @Bean
    public Step deleteNotificationsStep(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            DeleteNotificationReader deleteNotificationReader,
            DeleteNotificationWriter deleteNotificationWriter) {

        return new StepBuilder("deleteNotificationsStep", jobRepository)
                .<Long, Long>chunk(100, platformTransactionManager)
                .reader(deleteNotificationReader)
                .writer(deleteNotificationWriter)
                .build();
    }

    @Bean
    public Job deleteNotificationsJob(JobRepository jobRepository, Step deleteNotificationsStep) {
        return new JobBuilder("deleteNotificationsJob", jobRepository)
                .start(deleteNotificationsStep)
                .build();

    }
}