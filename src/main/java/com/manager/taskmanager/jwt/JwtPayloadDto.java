package com.manager.taskmanager.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtPayloadDto {
    private String department;
    private String employeeNumber;
    private String role;
    private Integer level;
}
