package com.manager.taskmanager.member.entity;

public enum Position {
    INTERN("인턴", 1), STAFF("사원", 1), PROFESSIONAL("주임", 2),
    ASSISTANT_MANAGER("대리", 3), GENERAL_MANAGER("과장", 3),
    DEPUTY_GENERAL_MANAGER("차장", 4), DEPARTMENT_HEAD("부장", 5), DIRECTOR("이사", 6),
    SENIOR_VICE_PRESIDENT("상무", 6), EXECUTIVE_VICE_PRESIDENT("전무", 6),
    VICE_PRESIDENT("부사장", 7), PRESIDENT("사장", 8),
    VICE_CHAIRMAN("부회장", 9), CHAIRMAN("회장", 10)
    ;

    private final String korean;
    private final int level;

    Position(String korean, int level)
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
