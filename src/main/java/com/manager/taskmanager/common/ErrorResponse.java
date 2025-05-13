package com.manager.taskmanager.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

public class ErrorResponse {
    private int status;
    private String message;
    private String code;
    private List<FieldError> errors;

    public ErrorResponse(int status, String message, String code, List<FieldError> errors) {
        this.status = status;
        this.message = message;
        this.code = code;
        this.errors = errors;
    }

    public static ErrorResponse of(HttpStatus status, String message, String code) {
        return new ErrorResponse(status.value(), message, code, null);
    }

    public static ErrorResponse of(HttpStatus status, String message, String code, List<FieldError> errors) {
        return new ErrorResponse(status.value(), message, code, errors);
    }

    @Getter
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String reason;
    }
}
