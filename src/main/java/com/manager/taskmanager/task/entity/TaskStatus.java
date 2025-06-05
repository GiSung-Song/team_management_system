package com.manager.taskmanager.task.entity;

import com.manager.taskmanager.common.CustomException;
import com.manager.taskmanager.common.ErrorCode;

public enum TaskStatus {
    PENDING, PROGRESS, COMPLETED, CANCELED;

    public static TaskStatus from(String status) {
        try {
            return TaskStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_STATUS);
        }
    }
}
