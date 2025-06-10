package com.manager.taskmanager.config;

import com.manager.taskmanager.department.DepartmentRepository;
import com.manager.taskmanager.department.entity.Department;
import com.manager.taskmanager.global.config.security.CustomUserDetails;
import com.manager.taskmanager.member.MemberRepository;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.member.entity.Position;
import com.manager.taskmanager.member.entity.Role;
import com.manager.taskmanager.notification.NotificationRepository;
import com.manager.taskmanager.notification.entity.Notification;
import com.manager.taskmanager.project.ProjectRepository;
import com.manager.taskmanager.project.entity.Project;
import com.manager.taskmanager.project.entity.ProjectStatus;
import com.manager.taskmanager.projectmember.ProjectMemberRepository;
import com.manager.taskmanager.projectmember.entity.ProjectMember;
import com.manager.taskmanager.projectmember.entity.ProjectRole;
import com.manager.taskmanager.task.TaskRepository;
import com.manager.taskmanager.task.entity.Task;
import com.manager.taskmanager.task.entity.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class TestDataFactory {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public Department createDepartment() {
        Department department = Department.builder()
                .departmentName("PJ-DV")
                .build();

        return departmentRepository.save(department);
    }

    public Member createMember(Department department) {
        Member member = Member.createMember(
                "emp-0002", "password", "member", "member@email.com",
                "01056785678", Position.ASSISTANT_MANAGER, department
        );

        return memberRepository.save(member);
    }

    public Member createManager(Department department) {
        Member manager = Member.createMember(
                "emp-0003", "password", "manager", "manager@email.com",
                "01013572468", Position.GENERAL_MANAGER, department
        );

        return memberRepository.save(manager);
    }

    public Member createLeader(Department department) {
        Member leader = Member.createMember(
                "emp-0001", "password", "leader", "leader@email.com",
                "01012341234", Position.DIRECTOR, department
        );

        return memberRepository.save(leader);
    }

    public Project createProject(Member leader, Member member) {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(5);
        LocalDate end = now.plusWeeks(15);

        Project project = Project.builder()
                .projectName("test-project")
                .description("test-description")
                .startDate(start)
                .endDate(end)
                .projectStatus(ProjectStatus.PROGRESS)
                .build();

        ProjectMember projectLeader = ProjectMember.createMember(
                leader, ProjectRole.LEADER, start, end
        );

        ProjectMember projectMember = ProjectMember.createMember(
                member, ProjectRole.MEMBER, now.plusWeeks(5), now.plusWeeks(10)
        );

        project.addProjectMember(projectLeader);
        project.addProjectMember(projectMember);

        return projectRepository.save(project);
    }

    public Task createTask(Member member, Project project) {
        ProjectMember projectMember = projectMemberRepository.findAll().stream()
                .filter(m -> m.getMember().getId().equals(member.getId()))
                .filter(m -> m.getProject().getId().equals(project.getId()))
                .findFirst()
                .orElseThrow();

        Task task = Task.createTask(
                projectMember, "task", "description",
                LocalDate.now().minusDays(3), LocalDate.now().plusWeeks(5),
                TaskStatus.PROGRESS
        );

        project.addTask(task);
        projectRepository.save(project);

        return project.getTasks().stream()
                .filter(t -> t.getTaskName().equalsIgnoreCase("task"))
                .findFirst()
                .orElseThrow();
    }

    public void createDeletedTask(Member member, Project project, LocalDateTime deletedAt) {
        project.updateProject(
                project.getDescription(),
                LocalDate.now().minusYears(2),
                LocalDate.now(),
                ProjectStatus.CANCELED
        );

        ProjectMember projectMember = projectMemberRepository.findAll().stream()
                .filter(m -> m.getMember().getId().equals(member.getId()))
                .filter(m -> m.getProject().getId().equals(project.getId()))
                .findFirst()
                .orElseThrow();

        Task task = Task.createTask(
                projectMember,
                "task",
                "description",
                LocalDate.now().minusYears(2),
                LocalDate.now(),
                TaskStatus.CANCELED
        );

        task.deleteTask();
        ReflectionTestUtils.setField(task, "deletedAt", deletedAt);

        project.addTask(task);

        projectRepository.save(project);
    }

    public void setAuthentication(Member member, Role role) {
        CustomUserDetails customUserDetails = new CustomUserDetails(member.getId(), member.getEmployeeNumber(), role.getValue());

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                null,
                List.of(new SimpleGrantedAuthority(role.getValue()))
        );

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    public Notification createNotification(Member member, String message, LocalDate date) {
        Notification notification = Notification.builder()
                .member(member)
                .message(message)
                .createdDate(date)
                .isRead(false)
                .build();

        return notificationRepository.save(notification);
    }

    public Notification createReadNotification(Member member, String message, LocalDate date) {
        Notification notification = Notification.builder()
                .member(member)
                .message(message)
                .createdDate(date)
                .isRead(true)
                .build();

        return notificationRepository.save(notification);
    }

    public void saveDueTodayAndPENDING(Member leader, Member member) {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(5);
        LocalDate end = now.plusWeeks(15);

        Project project = Project.builder()
                .projectName("test-project")
                .description("test-description")
                .startDate(start)
                .endDate(end)
                .projectStatus(ProjectStatus.PROGRESS)
                .build();

        ProjectMember projectLeader = ProjectMember.createMember(
                leader, ProjectRole.LEADER, start, end
        );

        ProjectMember projectMember = ProjectMember.createMember(
                member, ProjectRole.MEMBER, now.plusWeeks(5), now.plusWeeks(10)
        );

        project.addProjectMember(projectLeader);
        project.addProjectMember(projectMember);

        projectRepository.save(project);

        Task task = Task.createTask(
                projectMember, "task", "description",
                LocalDate.now().minusDays(3), LocalDate.now(),
                TaskStatus.PROGRESS
        );

        project.addTask(task);
        projectRepository.save(project);
    }

    public void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    public void clearAllData() {
        notificationRepository.deleteAllInBatch();
        taskRepository.deleteAllInBatch();
        projectMemberRepository.deleteAllInBatch();
        projectRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
    }
}
