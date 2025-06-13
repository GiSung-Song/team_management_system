package com.manager.taskmanager.global.log.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.taskmanager.auth.dto.LoginRequestDto;
import com.manager.taskmanager.global.config.security.CustomUserDetails;
import com.manager.taskmanager.global.log.annotation.SaveLogging;
import com.manager.taskmanager.global.log.dto.LogEvent;
import com.manager.taskmanager.member.dto.MemberRegisterDto;
import com.manager.taskmanager.member.dto.MemberUpdateDto;
import com.manager.taskmanager.member.dto.PasswordUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final ObjectMapper objectMapper;

    @Around("@annotation(saveLogging)")
    public Object saveLog(ProceedingJoinPoint joinPoint, SaveLogging saveLogging) throws Throwable {
        String eventName = saveLogging.eventName();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String employeeNumber = "GUEST";

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            employeeNumber = customUserDetails.getEmployeeNumber();
        }

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        Object[] args = joinPoint.getArgs();
        Object[] maskingArgs = makeMaskingData(args);

        long start = System.currentTimeMillis();
        Object result;

        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            LogEvent errorLog = LogEvent.error(
                    eventName,
                    className,
                    methodName,
                    employeeNumber,
                    maskingArgs,
                    e.getMessage()
            );

            String jsonData = objectMapper.writeValueAsString(errorLog);

            log.error(jsonData, e);

            throw e;
        }

        long duration = System.currentTimeMillis() - start;

        LogEvent successLog = LogEvent.success(
                eventName,
                className,
                methodName,
                employeeNumber,
                maskingArgs,
                result,
                duration
        );

        String jsonData = objectMapper.writeValueAsString(successLog);
        log.info(jsonData);

        return result;
    }

    private Object[] makeMaskingData(Object[] args) {
        return Arrays.stream(args)
                .map(this::makeMaskingObject)
                .toArray();
    }

    private Object makeMaskingObject(Object arg) {
        if (arg == null) {
            return null;
        }

        if (arg instanceof MemberRegisterDto dto) {
            return new MemberRegisterDto(
                    dto.getEmployeeNumber(),
                    "**********",
                    dto.getName(),
                    makeMaskingEmail(dto.getEmail()),
                    makeMaskingPhoneNumber(dto.getPhoneNumber()),
                    dto.getPosition(),
                    dto.getDepartmentId()
            );
        } else if (arg instanceof MemberUpdateDto dto) {
            return new MemberUpdateDto(
                    makeMaskingEmail(dto.getEmail()),
                    makeMaskingPhoneNumber(dto.getPhoneNumber()),
                    dto.getPosition(),
                    dto.getDepartmentId()
            );
        } else if (arg instanceof PasswordUpdateDto) {
            return new PasswordUpdateDto(
                    "**********"
            );
        } else if (arg instanceof LoginRequestDto dto) {
            return new LoginRequestDto(
                    dto.getEmployeeNumber(),
                    "**********"
            );
        }

        return arg;
    }

    private String makeMaskingEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        int idx = email.indexOf("@");
        String prefix = email.substring(0, Math.min(3, idx));

        return prefix + "****" + email.substring(idx);
    }

    private String makeMaskingPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 11) {
            return phoneNumber;
        }

        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }
}
