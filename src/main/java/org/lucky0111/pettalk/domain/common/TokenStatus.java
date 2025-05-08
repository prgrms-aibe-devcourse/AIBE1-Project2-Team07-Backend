package org.lucky0111.pettalk.domain.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenStatus {
    AUTHENTICATED("인가된 토큰"),
    EXPIRED("만료된 토큰"),
    INVALIDATED("잘못된 토큰");

    private final String description;
}
