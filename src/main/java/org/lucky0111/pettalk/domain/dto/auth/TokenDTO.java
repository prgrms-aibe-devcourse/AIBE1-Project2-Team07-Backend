package org.lucky0111.pettalk.domain.dto.auth;

public record TokenDTO(String accessToken, String refreshToken, long expiresIn) {

}