package com.manager.taskmanager.task;

import com.manager.taskmanager.common.CustomException;
import com.manager.taskmanager.common.ErrorCode;
import com.manager.taskmanager.project.entity.Project;
import com.manager.taskmanager.projectmember.ProjectMemberUtilService;
import com.manager.taskmanager.projectmember.entity.ProjectMember;
import com.manager.taskmanager.task.dto.*;
import com.manager.taskmanager.task.entity.Task;
import com.manager.taskmanager.task.entity.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskQueryRepository taskQueryRepository;
    private final ProjectMemberUtilService pmUtilService;

    // 추가
    @Transactional
    public void addTask(Long memberId, Long projectId, AddTaskDto dto) {
        ProjectMember projectMember = pmUtilService.getProjectMember(memberId, projectId);
        Project project = projectMember.getProject();

        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();

        pmUtilService.checkProjectDate(startDate, endDate, project.getStartDate(), project.getEndDate());

        Task task = Task.createTask(
                projectMember,
                dto.getTaskName(),
                dto.getDescription(),
                startDate,
                endDate,
                TaskStatus.valueOf(dto.getTaskStatus())
        );

        project.addTask(task);
    }

    // 수정
    @Transactional
    public void updateTask(Long memberId, Long taskId, UpdateTaskDto dto) {
        Task task = getTaskAndCheckOwner(memberId, taskId);

        task.updateTask(dto.getDescription(), dto.getStartDate(),
                dto.getEndDate(), TaskStatus.valueOf(dto.getTaskStatus()));
    }

    // 삭제
    @Transactional
    public void deleteTask(Long memberId, Long taskId) {
        Task task = getTaskAndCheckOwner(memberId, taskId);

        task.deleteTask();
    }

    // 업무 목록 조회
    @Transactional(readOnly = true)
    public TaskListDto getTaskList(Long memberId, boolean isManager, TaskSearchCondition condition) {
        List<Task> taskList = taskQueryRepository.getTaskList(memberId, isManager, condition);

        List<TaskListDto.TaskInfo> taskInfoList = taskList.stream()
                .map(task -> new TaskListDto.TaskInfo(
                        task.getProject().getProjectName(),
                        task.getTaskName(),
                        task.getTaskStatus().name(),
                        task.getDeletedAt()))
                .collect(Collectors.toList());

        return new TaskListDto(taskInfoList);
    }

    // 상세 조회
    @Transactional(readOnly = true)
    public TaskDetailDto getTaskDetail(Long memberId, Long taskId, boolean isManager) {
        Task task = null;

        if (isManager) {
            task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));
        } else {
            task = getTaskAndCheckOwner(memberId, taskId);
        }

        return new TaskDetailDto(
                task.getProject().getProjectName(),
                task.getTaskName(),
                task.getDescription(),
                task.getStartDate(),
                task.getEndDate(),
                task.getTaskStatus().name(),
                task.getDeletedAt()
        );
    }

    // 업무 조회 및 업무 권한 체크
    private Task getTaskAndCheckOwner(Long memberId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));

        ProjectMember projectMember = pmUtilService.getProjectMember(memberId, task.getProject().getId());

        if (!task.getProjectMember().getId().equals(projectMember.getId())) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        return task;
    }
}
