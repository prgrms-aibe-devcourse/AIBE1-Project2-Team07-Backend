package org.lucky0111.pettalk.domain.dto.admin;

import org.lucky0111.pettalk.domain.common.UserRole;

import java.util.UUID;

public record AdminUserDTO(UUID userId, String name, String nickname, String email, String profileImageUrl, UserRole role, String provider, String socialId, String status) {
}
