package org.lucky0111.pettalk.domain.common;

public enum UserRole {
    USER("일반 사용자"),
    //    PENDING_TRAINER("훈련사 신청 대기"),
    TRAINER("훈련사"),
    ADMIN("관리자");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}