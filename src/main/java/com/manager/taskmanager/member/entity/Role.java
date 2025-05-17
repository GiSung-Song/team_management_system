package com.manager.taskmanager.member.entity;

public enum Role {
    MANAGER("ROLE_MANAGER"), MEMBER("ROLE_MEMBER");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Role from(String value) {
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(value) || role.getValue().equalsIgnoreCase(value)) {
                return role;
            }
        }

        return null;
    }
}