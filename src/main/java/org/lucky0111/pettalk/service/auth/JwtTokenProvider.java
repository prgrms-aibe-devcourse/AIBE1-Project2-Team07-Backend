package org.lucky0111.pettalk.service.auth;

import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.common.TokenStatus;
import org.lucky0111.pettalk.domain.common.TokenType;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.entity.auth.RefreshToken;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.exception.CustomException;
import org.lucky0111.pettalk.repository.auth.RefreshTokenRepository;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.lucky0111.pettalk.util.auth.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtUtil jwtUtil;
    private final PetUserRepository petUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public String createAccessToken(Authentication authentication) throws RuntimeException {
        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
        UUID userId = UUID.fromString(oauthUser.getName());
        List<String> roles = extractRoles(authentication);
        return jwtUtil.createJwt(TokenType.ACCESS, userId, roles);
    }

    public String createRefreshToken(Authentication authentication) throws RuntimeException {
        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
        UUID userId = UUID.fromString(oauthUser.getName());
        List<String> roles = extractRoles(authentication);
        return jwtUtil.createJwt(TokenType.REFRESH, userId, roles);
    }

    @Transactional
    public String reissue(String refreshToken) throws CustomException {
        TokenStatus status = validateToken(refreshToken);

        switch (status) {
            case EXPIRED -> throw new CustomException("리프레시 토큰 만료. 로그인을 다시 해주세요.", HttpStatus.UNAUTHORIZED);
            case INVALIDATED -> throw new CustomException("리프레시 토큰이 유효하지 않습니다.", HttpStatus.FORBIDDEN);
        }

        saveRefreshToken(refreshToken);
        return jwtUtil.createJwt(TokenType.ACCESS, getUserId(refreshToken), getRoles(refreshToken));
    }

    @Transactional
    public void saveRefreshToken(String accessToken) throws CustomException {
        PetUser petUser = petUserRepository.findById(getUserId(accessToken))
                .orElseThrow(() -> new CustomException("유저를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST));

        RefreshToken refreshToken = refreshTokenRepository.findByUser(petUser)
                .orElse(new RefreshToken());

        refreshToken.setUser(petUser);
        refreshToken.setToken(accessToken);
        refreshToken.setExpiryDate(getTokenExpiryDate(accessToken));

        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public String deleteRefreshToken(String accessToken) throws CustomException {
        PetUser petUser = petUserRepository.findById(getUserId(accessToken))
                .orElseThrow(() -> new CustomException("유저를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST));

        TokenStatus status = validateToken(accessToken);

        switch (status) {
            case AUTHENTICATED -> {
                RefreshToken refreshToken = refreshTokenRepository.findByUser(petUser)
                        .orElseThrow(() -> new CustomException("리프레시 토큰을 찾을 수 없습니다.", HttpStatus.BAD_REQUEST));
                refreshTokenRepository.delete(refreshToken);
            }
            case EXPIRED -> throw new CustomException("엑세스 토큰 만료. 로그인을 다시 해주세요.", HttpStatus.UNAUTHORIZED);
            case INVALIDATED -> throw new CustomException("엑세스 토큰이 유효하지 않습니다.", HttpStatus.FORBIDDEN);
        }

        return jwtUtil.createJwt(TokenType.DELETE, getUserId(accessToken), getRoles(accessToken));
    }

    private List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    private LocalDateTime getTokenExpiryDate(String token) {
        Date expiration = getExpiration(token);
        return LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.of("Asia/Seoul"));
    }


    public TokenStatus validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public long getExpiresInSeconds(String token) throws RuntimeException {
        return jwtUtil.getExpiresInSeconds(token);
    }

    public Date getExpiration(String token) {
        return jwtUtil.getExpiration(token);
    }

    public UUID getUserId(String token) {
        return jwtUtil.getUserId(token);
    }

    public List<String> getRoles(String token) {
        return jwtUtil.getRoles(token);
    }
}
