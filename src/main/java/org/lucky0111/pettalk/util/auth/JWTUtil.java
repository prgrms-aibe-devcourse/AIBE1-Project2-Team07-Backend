package org.lucky0111.pettalk.util.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.dto.auth.OAuthTempTokenDTO;
import org.lucky0111.pettalk.domain.dto.auth.TokenDTO;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.domain.entity.auth.RefreshToken;
import org.lucky0111.pettalk.repository.auth.RefreshTokenRepository;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * JWT 서비스를 위한 파사드 클래스
 * 기존 코드와의 호환성을 위해 유지
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JWTUtil {

    private final JWTTokenProvider tokenProvider;
    private final JWTTokenManager tokenManager;

    // JWTTokenProvider에 위임
    public String getProvider(String token) {
        return tokenProvider.getProvider(token);
    }

    // JWTTokenProvider에 위임
    public String getSocialId(String token) {
        return tokenProvider.getSocialId(token);
    }

    // JWTTokenProvider에 위임
    public String getRole(String token) {
        return tokenProvider.getRole(token);
    }

    // JWTTokenProvider에 위임
    public UUID getUserId(String token) {
        return tokenProvider.getUserId(token);
    }

    // JWTTokenProvider에 위임
    public String getEmail(String token) {
        return tokenProvider.getEmail(token);
    }

    // JWTTokenProvider에 위임
    public Boolean isExpired(String token) {
        return tokenProvider.isExpired(token);
    }

    // JWTTokenProvider에 위임
    public long getExpiresIn(String token) {
        return tokenProvider.getExpiresIn(token);
    }

    // JWTTokenProvider에 위임
    public String createJwt(String provider, String socialId, UUID userId, String role, Long expiredMs) {
        return tokenProvider.createJwt(provider, socialId, userId, role, expiredMs);
    }

    // JWTTokenProvider에 위임
    public String createJwtWithEmail(String provider, String socialId, UUID userId, String role, String email, Long expiredMs) {
        return tokenProvider.createJwtWithEmail(provider, socialId, userId, role, email, expiredMs);
    }

    // JWTTokenProvider에 위임
    public String createTempToken(String provider, String providerId, String email, String name, Long expiredMs) {
        return tokenProvider.createTempToken(provider, providerId, email, name, expiredMs);
    }

    // JWTTokenManager에 위임
    public OAuthTempTokenDTO getTempTokenInfo(String token) {
        return tokenManager.getTempTokenInfo(token);
    }

    // JWTTokenManager에 위임
    public String generateRefreshToken(PetUser user) {
        return tokenManager.generateRefreshToken(user);
    }

    // JWTTokenManager에 위임
    public TokenDTO generateTokenPair(PetUser user) {
        return tokenManager.generateTokenPair(user);
    }

    // JWTTokenManager에 위임
    public Optional<TokenDTO> refreshAccessToken(String refreshToken) {
        return tokenManager.refreshAccessToken(refreshToken);
    }

    // JWTTokenManager에 위임
    public boolean revokeRefreshToken(String refreshToken) {
        return tokenManager.revokeRefreshToken(refreshToken);
    }

    // JWTTokenManager에 위임
    public void revokeAllUserTokens(UUID userId) {
        tokenManager.revokeAllUserTokens(userId);
    }

    // JWTTokenManager에 위임
    public void removeExpiredTokens() {
        tokenManager.removeExpiredTokens();
    }
}