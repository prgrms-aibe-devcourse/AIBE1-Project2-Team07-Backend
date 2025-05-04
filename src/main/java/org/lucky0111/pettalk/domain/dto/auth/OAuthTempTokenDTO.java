package org.lucky0111.pettalk.domain.dto.auth;

import lombok.Builder;

@Builder
public record OAuthTempTokenDTO(String provider, String providerId, String email, String name, boolean registrationCompleted) {

}