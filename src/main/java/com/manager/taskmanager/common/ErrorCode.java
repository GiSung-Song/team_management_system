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
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다."),
    PROJECT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_MEMBER_NOT_FOUND", "프로젝트에 속한 회원을 찾을 수 없습니다."),

    // INVALID
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "MISSING_REQUIRED_FIELD", "필수 입력 값이 누락되었습니다."),
    INVALID_PROJECT_STATUS(HttpStatus.BAD_REQUEST, "INVALID_PROJECT_STATUS", "프로젝트 상태 값이 유효하지 않습니다."),
    INVALID_PROJECT_DATE(HttpStatus.BAD_REQUEST, "INVALID_PROJECT_DATE", "프로젝트 시작날짜와 종료날짜를 확인해주세요."),

    // DUPLICATE
    DEPARTMENT_DUPLICATE(HttpStatus.CONFLICT, "DEPARTMENT_DUPLICATE", "이미 등록된 부서입니다."),
    EMPLOYEE_NUMBER_DUPLICATE(HttpStatus.CONFLICT, "EMPLOYEE_NUMBER_DUPLICATE", "이미 등록된 사번입니다."),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "EMAIL_DUPLICATE", "이미 등록된 이메일입니다."),
    PHONE_NUMBER_DUPLICATE(HttpStatus.CONFLICT, "PHONE_NUMBER_DUPLICATE", "이미 등록된 휴대폰 번호입니다."),

    // AUTH
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "아이디나 비밀번호가 틀렸습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "토큰이 유효하지 않습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "토큰이 만료되었습니다."),

    NO_PERMISSION(HttpStatus.FORBIDDEN, "NO_PERMISSION", "해당 작업에 대한 권한이 없습니다."),

    PROJECT_CANNOT_BE_DELETED(HttpStatus.BAD_REQUEST, "PROJECT_CANNOT_BE_DELETED", "이미 삭제되었거나 완료된 프로젝트입니다."),
    PROJECT_MEMBER_CANNOT_BE_DELETED(HttpStatus.BAD_REQUEST, "PROJECT_MEMBER_CANNOT_BE_DELETED", "삭제할 수 없는 회원입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
