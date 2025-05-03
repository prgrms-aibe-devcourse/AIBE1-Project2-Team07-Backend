package org.lucky0111.pettalk.domain.dto.auth;

public record UserRegistrationDTO(String tempToken, String name, String nickname, String profileImageUrl) {

}