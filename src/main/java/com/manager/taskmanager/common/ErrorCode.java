package com.manager.taskmanager.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // NOT_FOUND
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DEPARTMENT_NOT_FOUND", "부서를 찾을 수 없습니다."),

    // INVALID
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "MISSING_REQUIRED_FIELD", "필수 입력 값이 누락되었습니다."),

    // DUPLICATE
    DEPARTMENT_DUPLICATE(HttpStatus.BAD_REQUEST, "DEPARTMENT_DUPLICATE", "이미 등록된 부서입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
