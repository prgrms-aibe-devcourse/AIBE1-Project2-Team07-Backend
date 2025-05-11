package org.lucky0111.pettalk.service.auth;

import org.lucky0111.pettalk.domain.dto.auth.TokenDTO;

public interface AuthService {
    TokenDTO logout(String accessToken);

    TokenDTO reissue(String refreshToken);
}
