package com.manager.taskmanager.global.config.security;

import org.springframework.http.HttpMethod;

public record PermitPass(HttpMethod method, String pathPrefix) {

    public boolean matches(String reqMethod, String reqPath) {
        return (method == null || method.name().equalsIgnoreCase(reqMethod)) && reqPath.startsWith(pathPrefix);
    }
}