package org.lucky0111.pettalk.service.auth;

import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.common.TokenStatus;
import org.lucky0111.pettalk.domain.dto.auth.TokenDTO;
import org.lucky0111.pettalk.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public TokenDTO logout(String accessToken) {
        try {
            TokenStatus tokenStatus = jwtTokenProvider.validateToken(accessToken);

            switch (tokenStatus) {
                case EXPIRED -> throw new CustomException("엑세스 토큰 만료. 로그인을 다시 해주세요.", HttpStatus.UNAUTHORIZED);
                case INVALIDATED -> throw new CustomException("엑세스 토큰이 유효하지 않습니다.", HttpStatus.FORBIDDEN);
            }

            String deleteToken = jwtTokenProvider.deleteRefreshToken(accessToken);
            return TokenDTO.builder()
                    .accessToken(deleteToken)
                    .accessTokenExpiresIn(jwtTokenProvider.getExpiresInSeconds(deleteToken))
                    .refreshToken(deleteToken)
                    .refreshTokenExpiresIn(jwtTokenProvider.getExpiresInSeconds(deleteToken))
                    .build();
        } catch (Exception e) {
            throw new CustomException("로그아웃 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public TokenDTO reissue(String refreshToken) {
        try {
            TokenStatus tokenStatus = jwtTokenProvider.validateToken(refreshToken);

            switch (tokenStatus) {
                case EXPIRED -> throw new CustomException("리프레시 토큰 만료. 로그인을 다시 해주세요.", HttpStatus.UNAUTHORIZED);
                case INVALIDATED -> throw new CustomException("리프레시 토큰이 유효하지 않습니다.", HttpStatus.FORBIDDEN);
            }

            String accessToken = jwtTokenProvider.reissue(refreshToken);
            return TokenDTO.builder()
                    .accessToken(accessToken)
                    .accessTokenExpiresIn(jwtTokenProvider.getExpiresInSeconds(accessToken))
                    .refreshToken(refreshToken)
                    .refreshTokenExpiresIn(jwtTokenProvider.getExpiresInSeconds(refreshToken))
                    .build();
        } catch (Exception e) {
            throw new CustomException("리프레시 토큰 재발급 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
