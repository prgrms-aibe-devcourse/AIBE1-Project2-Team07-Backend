package org.lucky0111.pettalk.util.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰 생성 및 검증을 담당하는 클래스
 */
@Slf4j
@Component
public class JWTTokenProvider {

    private final SecretKey secretKey;

    @Value("${spring.jwt.access-token-expiration-ms:3600000}")
    private Long accessTokenExpirationMs; // Default 1 hour

    public JWTTokenProvider(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    /**
     * 기본 JWT 토큰을 생성합니다.
     */
    public String createJwt(String provider, String socialId, UUID userId, String role, Long expiredMs) {
        log.debug("JWT 토큰 생성 - 사용자: {} {}, 역할: {}", provider, socialId, role);
        try {
            String token = Jwts.builder()
                    .claim("provider", provider)
                    .claim("socialId", socialId)
                    .claim("userId", userId.toString())
                    .claim("role", role)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + expiredMs))
                    .signWith(secretKey)
                    .compact();
            log.debug("JWT 토큰 생성 성공");
            return token;
        } catch (Exception e) {
            log.error("JWT 토큰 생성 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 이메일 정보를 포함한 JWT 토큰을 생성합니다.
     */
    public String createJwtWithEmail(String provider, String socialId, UUID userId, String role, String email, Long expiredMs) {
        log.debug("JWT 토큰 생성 - 사용자: {} {}, 역할: {}, 이메일: {}", provider, socialId, role, email);
        try {
            String token = Jwts.builder()
                    .claim("provider", provider)
                    .claim("socialId", socialId)
                    .claim("userId", userId.toString())
                    .claim("role", role)
                    .claim("email", email)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + expiredMs))
                    .signWith(secretKey)
                    .compact();
            log.debug("JWT 토큰 생성 성공");
            return token;
        } catch (Exception e) {
            log.error("JWT 토큰 생성 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 임시 인증 토큰을 생성합니다.
     */
    public String createTempToken(String provider, String providerId, String email, String name, Long expiredMs) {
        log.debug("임시 인증 토큰 생성 - 제공자: {}, providerId: {}, email: {}", provider, providerId, email);
        try {
            String token = Jwts.builder()
                    .claim("provider", provider)
                    .claim("providerId", providerId)
                    .claim("email", email)
                    .claim("name", name)
                    .claim("registrationCompleted", false)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + expiredMs))
                    .signWith(secretKey)
                    .compact();
            log.debug("임시 인증 토큰 생성 성공");
            return token;
        } catch (Exception e) {
            log.error("임시 인증 토큰 생성 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 토큰에서 claims을 추출합니다.
     */
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("토큰 클레임 추출 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 토큰이 만료되었는지 확인합니다.
     */
    public boolean isExpired(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (claims == null) return true;

            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("토큰 만료 확인 중 오류 발생: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 토큰의 남은 만료 시간을 초 단위로 반환합니다.
     */
    public long getExpiresIn(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (claims == null) return 0;

            Date expiration = claims.getExpiration();
            Date now = new Date();

            long diff = expiration.getTime() - now.getTime();
            return Math.max(0, diff / 1000);
        } catch (Exception e) {
            log.error("토큰 만료 시간 계산 중 오류 발생: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 토큰에서 provider를 추출합니다.
     */
    public String getProvider(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.get("provider", String.class) : null;
    }

    /**
     * 토큰에서 socialId를 추출합니다.
     */
    public String getSocialId(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.get("socialId", String.class) : null;
    }

    /**
     * 토큰에서 role을 추출합니다.
     */
    public String getRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.get("role", String.class) : null;
    }

    /**
     * 토큰에서 userId를 추출합니다.
     */
    public UUID getUserId(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) return null;

        String userIdStr = claims.get("userId", String.class);
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }

    /**
     * 토큰에서 email을 추출합니다.
     */
    public String getEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.get("email", String.class) : null;
    }
}