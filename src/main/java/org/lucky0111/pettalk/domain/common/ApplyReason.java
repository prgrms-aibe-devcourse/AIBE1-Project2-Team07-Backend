package org.lucky0111.pettalk.domain.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplyReason {
    ACCEPTED("승인되었습니다"),
    NOT_EXPERTISE("전문 분야가 아닙니다"),
    SCHEDULE_UNAVAILABLE("현재 일정상 상담이 어렵습니다"),
    INSUFFICIENT_INFORMATION("상담에 필요한 정보가 부족합니다"),
    POLICY_VIOLATION("상담 규정에 적합하지 않습니다"),
    OTHER("기타 사유");

    private final String description;
}
