package com.manager.taskmanager.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // NOT_FOUND
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DEPARTMENT_NOT_FOUND", "부서를 찾을 수 없습니다."),

    // INVALID
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "MISSING_REQUIRED_FIELD", "필수 입력 값이 누락되었습니다."),

    // DUPLICATE
    DEPARTMENT_DUPLICATE(HttpStatus.CONFLICT, "DEPARTMENT_DUPLICATE", "이미 등록된 부서입니다."),
    EMPLOYEE_NUMBER_DUPLICATE(HttpStatus.CONFLICT, "EMPLOYEE_NUMBER_DUPLICATE", "이미 등록된 사번입니다."),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "EMAIL_DUPLICATE", "이미 등록된 이메일입니다."),
    PHONE_NUMBER_DUPLICATE(HttpStatus.CONFLICT, "PHONE_NUMBER_DUPLICATE", "이미 등록된 휴대폰 번호입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
