package com.manager.taskmanager.batch;

import com.manager.taskmanager.config.DBContainerExtension;
import com.manager.taskmanager.config.TestDataFactory;
import com.manager.taskmanager.department.entity.Department;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.notification.NotificationRepository;
import com.manager.taskmanager.project.entity.Project;
import com.manager.taskmanager.task.TaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(DBContainerExtension.class)
public class AllBatchTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job deleteOldTasksJob;

    @Autowired
    private Job deleteNotificationsJob;

    @Autowired
    private Job saveNotificationsJob;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private NotificationRepository notificationRepository;

    private Department department;
    private Member leader;
    private Member member;
    private Member manager;
    private Project project;

    @BeforeEach
    void setUp() {
        department = testDataFactory.createDepartment();
        leader = testDataFactory.createLeader(department);
        member = testDataFactory.createMember(department);
        manager = testDataFactory.createManager(department);
        project = testDataFactory.createProject(leader, member);
    }

    @AfterEach
    void clearDB() {
        testDataFactory.clearAllData();
    }

    @BeforeEach
    void init(@Autowired DataSource dataSource) throws SQLException, IOException {
        Resource resource = new ClassPathResource("org/springframework/batch/core/schema-mysql.sql");
        String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sqlStatement : sql.split(";")) {
                if (!sqlStatement.trim().isEmpty()) {
                    stmt.execute(sqlStatement);
                }
            }
        }
    }

    @Test
    void 오래된_업무_삭제_배치() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        testDataFactory.createDeletedTask(member, project, LocalDateTime.now().minusMonths(4));
        testDataFactory.createDeletedTask(leader, project, LocalDateTime.now().minusMonths(5));
        testDataFactory.createDeletedTask(leader, project, LocalDateTime.now().minusMonths(6));

        JobExecution execution = jobLauncher.run(
                deleteOldTasksJob,
                new JobParametersBuilder().addLong("run.id", System.currentTimeMillis()).toJobParameters()
        );

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(taskRepository.findAll()).isEmpty();
    }

    @Test
    void 업무_알림_저장_배치() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        testDataFactory.saveDueTodayAndPENDING(leader, member);

        JobExecution execution = jobLauncher.run(
                saveNotificationsJob,
                new JobParametersBuilder().addLong("run.id", System.currentTimeMillis()).toJobParameters()
        );

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(notificationRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void 업무_알림_자동_삭제_배치() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        testDataFactory.createReadNotification(member, "오래된 메시지입니다.", LocalDate.now().minusMonths(2));

        JobExecution execution = jobLauncher.run(
                deleteNotificationsJob,
                new JobParametersBuilder().addLong("run.id", System.currentTimeMillis()).toJobParameters()
        );

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(notificationRepository.findAll()).isEmpty();
    }
}
