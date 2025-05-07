package org.lucky0111.pettalk.domain.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplyStatus {
    PENDING,
    APPROVED,
    REJECTED;
}