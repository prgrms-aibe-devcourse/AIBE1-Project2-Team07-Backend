package org.lucky0111.pettalk.domain.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDTO {
    private String tempToken;
    private String name;
    private String nickname;
    private String profileImageUrl;
}