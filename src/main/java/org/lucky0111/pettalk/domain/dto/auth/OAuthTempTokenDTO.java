package org.lucky0111.pettalk.domain.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OAuthTempTokenDTO {
    private String provider;
    private String providerId;
    private String email;
    private String name;
    private boolean registrationCompleted;
}