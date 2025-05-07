package org.lucky0111.pettalk.domain.dto.user;

public record UserResponseDTO(UserInfo userInfo, TokenInfo tokenInfo) {
    public record UserInfo(String name, String nickname, String email, String profileImageUrl) {

    }

    public record TokenInfo(String accessToken, String refreshToken) {

    }
}
