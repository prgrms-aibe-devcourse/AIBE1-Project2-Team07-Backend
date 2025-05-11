package org.lucky0111.pettalk.domain.dto.auth;

import lombok.Builder;

@Builder
public record TokenDTO(String accessToken, String refreshToken, long accessTokenExpiresIn, long refreshTokenExpiresIn) {
}
