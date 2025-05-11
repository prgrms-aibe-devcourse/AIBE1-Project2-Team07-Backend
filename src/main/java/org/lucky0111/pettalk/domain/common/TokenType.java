package org.lucky0111.pettalk.domain.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenType {
    ACCESS("accessToken"),
    REFRESH("refreshToken"),
    DELETE("deleteToken");
    private final String name;
}
