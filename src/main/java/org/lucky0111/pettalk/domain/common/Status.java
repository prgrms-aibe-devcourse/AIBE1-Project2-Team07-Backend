package org.lucky0111.pettalk.domain.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    PENDING("신청중"),
    APPROVED("승인"),
    REJECTED("미승인");

    private final String description;
}