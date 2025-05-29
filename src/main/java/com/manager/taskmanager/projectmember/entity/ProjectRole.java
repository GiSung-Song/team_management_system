package com.manager.taskmanager.projectmember.entity;

public enum ProjectRole {
    LEADER("리더",3),
    MANAGER("매니저", 2),
    MEMBER("멤버", 1);

    private final String korean;
    private final int level;

    ProjectRole(String korean, int level)
    {
        this.korean = korean;
        this.level = level;
    }

    public String getKorean() {
        return korean;
    }

    public int getLevel() {
        return level;
    }
}
