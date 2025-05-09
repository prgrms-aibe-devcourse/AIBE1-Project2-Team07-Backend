package org.lucky0111.pettalk.service.token;

import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.common.TokenStatus;
import org.lucky0111.pettalk.service.auth.JwtTokenProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public String reissue(String accessToken) {
        TokenStatus tokenStatus = jwtTokenProvider.validateToken(accessToken);

        switch (tokenStatus) {
//            case AUTHENTICATED ->
        }

        return "";
    }

    private String createAccessToken() {
        return "";
        //        return jwtTokenProvider.createAccessToken()
    }
}
