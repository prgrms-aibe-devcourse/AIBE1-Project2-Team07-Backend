package org.lucky0111.pettalk.util.auth;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.config.auth.JWTConfig;
import org.lucky0111.pettalk.domain.common.TokenStatus;
import org.lucky0111.pettalk.domain.common.TokenType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JWTConfig jwtConfig;

    /**
     * JWT 토큰을 생성합니다.
     */
    public String createJwt(TokenType tokenType, UUID userId, List<String> roles) throws RuntimeException {
        try {
            Instant now = Instant.now();
            Date issuedAt = Date.from(now);
            Date expiration = getExpiration(tokenType);

            String token = Jwts.builder()
                    .claim("userId", userId.toString())
                    .claim("roles", roles)
                    .claim("type", tokenType.toString().toLowerCase())
                    .issuedAt(issuedAt)
                    .expiration(expiration)
                    .signWith(jwtConfig.getSecretKey())
                    .compact();

            log.info("{} 토큰 생성 성공", tokenType.getName());
            return token;
        } catch (Exception e) {
            log.error("JWT 토큰 생성 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("JWT 토큰 생성 중 오류 발생", e);
        }
    }

    private Date getExpiration(TokenType tokenType) {
        return switch (tokenType) {
            case ACCESS -> Date.from(Instant.now().plusMillis(jwtConfig.getAccessTokenExpiresIn()));
            case REFRESH ->
                    Date.from(Instant.now().plusMillis(jwtConfig.getRefreshTokenExpiresInDays() * 24 * 60 * 60 * 1000L));
            case DELETE -> Date.from(Instant.now().plusMillis(1000L));
        };
    }

    /**
     * 토큰에서 claims을 추출합니다.
     */
    public Claims extractAllClaims(String token) throws JwtException, IllegalArgumentException {
        return Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰 유효성 검사
     */
    public TokenStatus validateToken(String token) {
        try {
            Jwts.parser().verifyWith(jwtConfig.getSecretKey()).build().parseSignedClaims(token);
            return TokenStatus.AUTHENTICATED;
        } catch (SignatureException e) {
            log.info("Token Signature Invalid: {}", e.getMessage());
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Token Invalid: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.info("Token Expired: {}", e.getMessage());
            return TokenStatus.EXPIRED;
        } catch (UnsupportedJwtException e) {
            log.info("Token Unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.info("Token Illegal Argument: {}", e.getMessage());
        } catch (NullPointerException e) {
            log.info("Token is Null: {}", e.getMessage());
        }

        return TokenStatus.INVALIDATED;
    }

    /**
     * 토큰의 남은 만료 시간을 초 단위로 반환합니다.
     */
    public long getExpiresInSeconds(String token) throws RuntimeException {
        try {
            Claims claims = extractAllClaims(token);
            if (claims == null) return 0;

            Date expiration = claims.getExpiration();
            Instant now = Instant.now();
            Date nowDate = Date.from(now);

            long diff = expiration.getTime() - nowDate.getTime();
            return Math.max(0, diff / 1000);
        } catch (Exception e) {
            log.error("토큰 만료 시간 계산 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("토큰 만료 시간 계산 중 오류 발생", e);
        }
    }

    public Date getExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * 토큰에서 role을 추출합니다.
     */
    public List<String> getRoles(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("roles");
    }

    /**
     * 토큰에서 userId를 추출합니다.
     */
    public UUID getUserId(String token) {
        Claims claims = extractAllClaims(token);
        String userIdStr = claims.get("userId", String.class);
        return UUID.fromString(userIdStr);
    }
}