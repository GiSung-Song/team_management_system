package com.manager.taskmanager.member.entity;

public enum Position {
    INTERN("인턴", 1), STAFF("사원", 1), PROFESSIONAL("주임", 2),
    ASSISTANT_MANAGER("대리", 3), GENERAL_MANAGER("과장", 3),
    DEPUTY_GENERAL_MANAGER("차장", 4), DEPARTMENT_HEAD("부장", 4), DIRECTOR("이사", 5),
    SENIOR_VICE_PRESIDENT("상무", 5), EXECUTIVE_VICE_PRESIDENT("전무", 5),
    VICE_PRESIDENT("부사장", 6), PRESIDENT("사장", 6),
    VICE_CHAIRMAN("부회장", 7), CHAIRMAN("회장", 8)
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
