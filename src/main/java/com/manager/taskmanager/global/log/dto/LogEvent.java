package com.manager.taskmanager.global.log.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class LogEvent {
    private String eventName;
    private String message;
    private String timestamp;
    private String employeeNumber;
    private String className;
    private String methodName;
    private long duration;
    private Object[] args;
    private Object result;
    private String logLevel;

    public static LogEvent success(String eventName, String className, String methodName,
                                   String employeeNumber, Object[] args, Object result, long duration) {
        return LogEvent.builder()
                .eventName(eventName)
                .message("Success")
                .timestamp(ZonedDateTime.now().toString())
                .employeeNumber(employeeNumber)
                .className(className)
                .methodName(methodName)
                .args(args)
                .result(result)
                .duration(duration)
                .logLevel("INFO")
                .build();
    }

    public static LogEvent error(String eventName, String className, String methodName,
                                 String employeeNumber, Object[] args, String errorMessage) {
        return LogEvent.builder()
                .eventName(eventName)
                .message("Error : " + errorMessage)
                .timestamp(ZonedDateTime.now().toString())
                .employeeNumber(employeeNumber)
                .className(className)
                .methodName(methodName)
                .args(args)
                .logLevel("ERROR")
                .build();
    }
}
