package com.manager.taskmanager.config.security;

public class CustomUserDetails {

    private final Long id;
    private final String employeeNumber;
    private final String role;

    public CustomUserDetails(Long id, String employeeNumber, String role) {
        this.id = id;
        this.employeeNumber = employeeNumber;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getRole() {
        return role;
    }
}
