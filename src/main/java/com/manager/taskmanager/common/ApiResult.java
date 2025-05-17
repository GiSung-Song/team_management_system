package com.manager.taskmanager.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResult<T> {

    private int status;
    private String message;
    private T data;

    public ApiResult(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResult<T> success(HttpStatus status, String message, T data) {
        return new ApiResult<>(status.value(), message, data);
    }
}