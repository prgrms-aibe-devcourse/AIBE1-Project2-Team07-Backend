package org.lucky0111.pettalk.domain.common;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AccountStatus {
    ACTIVE("활성화"),
    INACTIVE("계정정지"),
    WITHDRAWN("회원탈퇴");

    private final String description;
}
