package com.manager.taskmanager.config.security;

import com.manager.taskmanager.member.entity.Position;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtPayloadDto {
    private Long id;
    private String department;
    private String employeeNumber;
    private String role;
    private Position position;
}
