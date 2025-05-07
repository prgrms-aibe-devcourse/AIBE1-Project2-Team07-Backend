package org.lucky0111.pettalk.util.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.dto.auth.OAuthTempTokenDTO;
import org.lucky0111.pettalk.domain.dto.auth.TokenDTO;
import org.lucky0111.pettalk.domain.entity.auth.RefreshToken;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.repository.auth.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * JWT 토큰 관리를 담당하는 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JWTTokenManager {

    private final JWTTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${spring.jwt.access-token-expiration-ms:3600000}")
    private Long accessTokenExpirationMs; // Default 1 hour

    @Value("${spring.jwt.refresh-token-expiration-days:30}")
    private Integer refreshTokenExpirationDays; // Default 30 days

    /**
     * 임시 토큰 정보를 추출합니다.
     */
    public OAuthTempTokenDTO getTempTokenInfo(String token) {
        try {
            var claims = tokenProvider.extractAllClaims(token);
            if (claims == null) return null;

            String email = claims.get("email", String.class);
            log.debug("토큰에서 추출한 이메일: {}", email);

            return OAuthTempTokenDTO.builder()
                    .provider(claims.get("provider", String.class))
                    .providerId(claims.get("providerId", String.class))
                    .email(email)
                    .name(claims.get("name", String.class))
                    .registrationCompleted(claims.get("registrationCompleted", Boolean.class))
                    .build();
        } catch (Exception e) {
            log.error("임시 토큰 파싱 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 새 리프레시 토큰을 생성합니다.
     */
    @Transactional
    public String generateRefreshToken(PetUser user) {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        LocalDateTime expiryDate = LocalDateTime.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

        RefreshToken refreshToken = new RefreshToken(token, user, expiryDate);
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    /**
     * 액세스 토큰과 리프레시 토큰을 생성합니다.
     */
    @Transactional
    public TokenDTO generateTokenPair(PetUser user) {
        String accessToken;
        if (user.getEmail() != null) {
            accessToken = tokenProvider.createJwtWithEmail(
                    user.getProvider(),
                    user.getSocialId(),
                    user.getUserId(),
                    user.getRole().name(),
                    user.getEmail(),
                    accessTokenExpirationMs);
        } else {
            accessToken = tokenProvider.createJwt(
                    user.getProvider(),
                    user.getSocialId(),
                    user.getUserId(),
                    user.getRole().name(),
                    accessTokenExpirationMs);
        }

        String refreshToken = generateRefreshToken(user);

        return new TokenDTO(accessToken, refreshToken, accessTokenExpirationMs / 1000);
    }

    /**
     * 리프레시 토큰을 검증하고 새 액세스 토큰을 생성합니다.
     */
    @Transactional
    public Optional<TokenDTO> refreshAccessToken(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .filter(RefreshToken::isValid)
                .map(token -> {
                    PetUser user = token.getUser();

                    // Create new access token with email
                    String newAccessToken;
                    if (user.getEmail() != null) {
                        newAccessToken = tokenProvider.createJwtWithEmail(
                                user.getProvider(),
                                user.getSocialId(),
                                user.getUserId(),
                                user.getRole().name(),
                                user.getEmail(),
                                accessTokenExpirationMs);
                    } else {
                        newAccessToken = tokenProvider.createJwt(
                                user.getProvider(),
                                user.getSocialId(),
                                user.getUserId(),
                                user.getRole().name(),
                                accessTokenExpirationMs);
                    }

                    // Return tokens
                    return new TokenDTO(newAccessToken, refreshToken, accessTokenExpirationMs / 1000);
                });
    }

    /**
     * 리프레시 토큰을 폐기합니다.
     */
    @Transactional
    public boolean revokeRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .map(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    return true;
                })
                .orElse(false);
    }

    /**
     * 사용자의 모든 리프레시 토큰을 폐기합니다.
     */
    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUser(userId);
    }

    /**
     * 만료된 토큰을 제거합니다.
     */
    @Transactional
    public void removeExpiredTokens() {
        refreshTokenRepository.deleteAllExpiredTokens(LocalDateTime.now());
    }
}