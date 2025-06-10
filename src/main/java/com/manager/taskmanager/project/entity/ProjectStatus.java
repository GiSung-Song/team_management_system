package com.manager.taskmanager.project.entity;

import com.manager.taskmanager.global.error.CustomException;
import com.manager.taskmanager.global.error.ErrorCode;

public enum ProjectStatus {
    PENDING, PROGRESS, COMPLETED, CANCELED;

    public static ProjectStatus from(String status) {
        try {
            return ProjectStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_STATUS);
        }
    }
}