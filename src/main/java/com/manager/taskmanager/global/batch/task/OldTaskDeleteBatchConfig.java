package com.manager.taskmanager.global.batch.task;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@EnableBatchProcessing
public class OldTaskDeleteBatchConfig {

    @Bean
    public Step deleteOldTasksStep(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            OldTaskDeleteReader oldTaskDeleteReader,
            OldTaskDeleteWriter oldTaskDeleteWriter) {

        return new StepBuilder("deleteOldTasksStep", jobRepository)
                .<Long, Long>chunk(100, platformTransactionManager)
                .reader(oldTaskDeleteReader)
                .writer(oldTaskDeleteWriter)
                .build();
    }

    @Bean
    public Job deleteOldTasksJob(JobRepository jobRepository, Step deleteOldTasksStep) {
        return new JobBuilder("deleteOldTasksJob", jobRepository)
                .start(deleteOldTasksStep)
                .build();

    }
}
