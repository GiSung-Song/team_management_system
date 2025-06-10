package com.manager.taskmanager.task;

import com.manager.taskmanager.global.error.CustomException;
import com.manager.taskmanager.global.error.ErrorCode;
import com.manager.taskmanager.config.DBContainerExtension;
import com.manager.taskmanager.config.TestDataFactory;
import com.manager.taskmanager.department.entity.Department;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.project.entity.Project;
import com.manager.taskmanager.task.dto.*;
import com.manager.taskmanager.task.entity.Task;
import com.manager.taskmanager.task.entity.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@ExtendWith(DBContainerExtension.class)
class TaskServiceTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    private Department department;
    private Member leader;
    private Member member;
    private Member manager;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        department = testDataFactory.createDepartment();
        leader = testDataFactory.createLeader(department);
        member = testDataFactory.createMember(department);
        manager = testDataFactory.createManager(department);
        project = testDataFactory.createProject(leader, member);
        task = testDataFactory.createTask(member, project);
    }

    @Nested
    @DisplayName("업무 추가 Service")
    class 업무_추가_서비스_테스트 {

        @Test
        @DisplayName("업무 추가 정상")
        void whenValidInput_thenTaskIsAdded() {
            AddTaskDto taskDto = new AddTaskDto(
                    "test-task", "task-description", LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(10), "PROGRESS"
            );

            taskService.addTask(member.getId(), project.getId(), taskDto);

            Task task = findByTaskName(taskDto.getTaskName());

            assertThat(task.getDescription()).isEqualTo(taskDto.getDescription());
            assertThat(task.getTaskName()).isEqualTo(taskDto.getTaskName());
            assertThat(task.getTaskStatus().name()).isEqualTo(taskDto.getTaskStatus());
        }

        @Test
        @DisplayName("프로젝트 날짜 범위가 아닐 시 400 반환")
        void whenInvalidTaskDate_thenReturnBadRequest() {
            AddTaskDto taskDto = new AddTaskDto(
                    "test-task", "task-description", LocalDate.now().minusMonths(5),
                    LocalDate.now().plusDays(10), "PROGRESS"
            );

            assertThatThrownBy(() -> taskService.addTask(member.getId(), project.getId(), taskDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_PROJECT_DATE);
                    });
        }

        @Test
        @DisplayName("프로젝트에 속하지 않은 멤버일 시 404 반환")
        void whenMemberNotInProject_thenReturnNotFound() {
            AddTaskDto taskDto = new AddTaskDto(
                    "test-task", "task-description", LocalDate.now(),
                    LocalDate.now().plusDays(5), "PROGRESS"
            );

            assertThatThrownBy(() -> taskService.addTask(manager.getId(), project.getId(), taskDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROJECT_MEMBER_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("업무 수정 Service")
    class 업무_수정_서비스_테스트 {

        @Test
        @DisplayName("업무 수정 정상")
        void whenValidInput_thenTaskIsUpdated() {
            UpdateTaskDto updateTaskDto =
                    new UpdateTaskDto("update-description", LocalDate.now(), LocalDate.now().plusDays(5), TaskStatus.CANCELED.name());
            taskService.updateTask(member.getId(), task.getId(), updateTaskDto);

            Task findTask = findByTaskName("task");

            assertThat(findTask.getTaskStatus()).isEqualTo(TaskStatus.CANCELED);
            assertThat(findTask.getDescription()).isEqualTo(updateTaskDto.getDescription());
        }

        @Test
        @DisplayName("자신의 업무가 아닐 시 403 반환")
        void whenNotTaskOwner_thenReturnForbidden() {
            UpdateTaskDto updateTaskDto =
                    new UpdateTaskDto("update-description", LocalDate.now(), LocalDate.now().plusDays(5), TaskStatus.CANCELED.name());

            assertThatThrownBy(() -> taskService.updateTask(leader.getId(), task.getId(), updateTaskDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_PERMISSION);
                    });
        }

        @Test
        @DisplayName("업무가 없을 시 404 반환")
        void whenNotFoundTask_thenReturnNotFound() {
            UpdateTaskDto updateTaskDto =
                    new UpdateTaskDto("update-description", LocalDate.now(), LocalDate.now().plusDays(5), TaskStatus.CANCELED.name());

            assertThatThrownBy(() -> taskService.updateTask(member.getId(), 43214321L, updateTaskDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TASK_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("프로젝트 멤버 아닐 시 404 반환")
        void whenMemberNotInProject_thenReturnNotFound() {
            UpdateTaskDto updateTaskDto =
                    new UpdateTaskDto("update-description", LocalDate.now(), LocalDate.now().plusDays(5), TaskStatus.CANCELED.name());

            assertThatThrownBy(() -> taskService.updateTask(manager.getId(), task.getId(), updateTaskDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROJECT_MEMBER_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("업무 삭제 Service")
    class 업무_삭제_서비스_테스트 {

        @Test
        @DisplayName("업무 삭제 정상")
        void whenValidInput_thenReturnTaskList() {
            taskService.deleteTask(member.getId(), task.getId());

            Task findTask = findByTaskName("task");

            assertThat(findTask.getDeletedAt()).isNotNull();
            assertThat(findTask.getTaskStatus()).isEqualTo(TaskStatus.CANCELED);
        }

        @Test
        @DisplayName("자신의 업무가 아닐 시 403 반환")
        void whenNotTaskOwner_thenReturnForbidden() {
            assertThatThrownBy(() -> taskService.deleteTask(leader.getId(), task.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_PERMISSION);
                    });
        }

        @Test
        @DisplayName("업무가 없을 시 404 반환")
        void whenNotFoundTask_thenReturnNotFound() {
            assertThatThrownBy(() -> taskService.deleteTask(member.getId(), 43214321L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TASK_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("프로젝트 멤버 아닐 시 404 반환")
        void whenMemberNotInProject_thenReturnNotFound() {
            assertThatThrownBy(() -> taskService.deleteTask(manager.getId(), task.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROJECT_MEMBER_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("업무 목록 조회 Service")
    class 업무_목록_조회_서비스_테스트 {

        @Test
        @DisplayName("업무 목록 조회 정상 - 프로젝트명/자기자신")
        void whenValidInputProjectName_thenReturnTaskList() {
            TaskListDto taskList =
                    taskService.getTaskList(member.getId(), false, new TaskSearchCondition("project", null, null));

            assertThat(taskList.getTaskList())
                    .hasSize(1)
                    .extracting("taskName")
                    .containsExactlyInAnyOrder("task");
        }

        @Test
        @DisplayName("업무 목록 조회 정상 - 업무상태/매니저")
        void whenValidInputTaskStatusForManager_thenReturnTaskList() {
            TaskListDto taskList =
                    taskService.getTaskList(leader.getId(), true, new TaskSearchCondition(null, null, TaskStatus.PROGRESS.name()));

            assertThat(taskList.getTaskList())
                    .hasSize(1)
                    .extracting("taskName")
                    .containsExactlyInAnyOrder("task");
        }
    }

    @Nested
    @DisplayName("업무 상세 조회 Service")
    class 업무_상세_조회_서비스_테스트 {

        @Test
        @DisplayName("업무 상세 조회 정상")
        void whenValidInput_thenReturnTaskDetail() {
            TaskDetailDto taskDetail = taskService.getTaskDetail(member.getId(), task.getId(), false);

            assertThat(taskDetail.getTaskName()).isEqualTo(task.getTaskName());
            assertThat(taskDetail.getProjectName()).isEqualTo(task.getProject().getProjectName());
        }

        @Test
        @DisplayName("자신의 업무가 아닐 시 403 반환")
        void whenNotTaskOwner_thenReturnForbidden() {
            assertThatThrownBy(() -> taskService.getTaskDetail(leader.getId(), task.getId(), false))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_PERMISSION);
                    });
        }

        @Test
        @DisplayName("업무가 없을 시 404 반환")
        void whenNotFoundTask_thenReturnNotFound() {
            assertThatThrownBy(() -> taskService.getTaskDetail(member.getId(), 43214321L, false))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TASK_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("프로젝트 멤버 아닐 시 404 반환")
        void whenMemberNotInProject_thenReturnNotFound() {
            assertThatThrownBy(() -> taskService.getTaskDetail(manager.getId(), task.getId(), false))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROJECT_MEMBER_NOT_FOUND);
                    });
        }
    }

    private Task findByTaskName(String taskName) {
        return taskRepository.findAll().stream()
                .filter(task -> task.getTaskName().equalsIgnoreCase(taskName))
                .findFirst()
                .orElseThrow();
    }

}