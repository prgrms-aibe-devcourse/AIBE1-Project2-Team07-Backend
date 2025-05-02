package org.lucky0111.pettalk.util.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
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

@Component
public class JWTUtil {

    private final SecretKey secretKey;
    private final PetUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${spring.jwt.access-token-expiration-ms:3600000}")
    private Long accessTokenExpirationMs; // Default 1 hour

    @Value("${spring.jwt.refresh-token-expiration-days:30}")
    private Integer refreshTokenExpirationDays; // Default 30 days

    public JWTUtil(@Value("${spring.jwt.secret}") String secret,
                   PetUserRepository userRepository,
                   RefreshTokenRepository refreshTokenRepository) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String getProvider(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("provider", String.class);
    }

    public String getSocialId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("socialId", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("role", String.class);
    }

    public UUID getUserId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("userId", UUID.class);

    }

    public String getEmail(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("email", String.class);
    }

    public Boolean isExpired(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                    .getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // 토큰의 남은 만료 시간을 초 단위로 반환하는 새로운 메서드
    public long getExpiresIn(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token).getPayload();

            Date expiration = claims.getExpiration();
            Date now = new Date();

            // 만료 시간과 현재 시간의 차이를 초 단위로 계산
            long diff = expiration.getTime() - now.getTime();
            return Math.max(0, diff / 1000); // 음수가 되지 않도록 최소값 0 설정
        } catch (Exception e) {
            return 0; // 오류 발생 시 만료된 것으로 처리
        }
    }

    public String createJwt(String provider, String socialId, UUID userId, String role, Long expiredMs) {
        System.out.println("JWT 토큰 생성 - 사용자: " + provider + " " + socialId + ", 역할: " + role);
        try {
            String token = Jwts.builder()
                    .claim("provider", provider)
                    .claim("socialId", socialId)
                    .claim("userId", userId)
                    .claim("role", role)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + expiredMs))
                    .signWith(secretKey)
                    .compact();
            System.out.println("JWT 토큰 생성 성공: " + token.substring(0, Math.min(token.length(), 10)) + "...");
            return token;
        } catch (Exception e) {
            System.out.println("JWT 토큰 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String createJwtWithEmail(String provider, String socialId, UUID userId, String role, String email, Long expiredMs) {
        System.out.println("JWT 토큰 생성 - 사용자: " + provider + " " + socialId + ", 역할: " + role + ", 이메일: " + email);
        try {
            String token = Jwts.builder()
                    .claim("provider", provider)
                    .claim("socialId", socialId)
                    .claim("userId", userId)
                    .claim("role", role)
                    .claim("email", email)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + expiredMs))
                    .signWith(secretKey)
                    .compact();
            System.out.println("JWT 토큰 생성 성공: " + token.substring(0, Math.min(token.length(), 10)) + "...");
            return token;
        } catch (Exception e) {
            System.out.println("JWT 토큰 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String createTempToken(String provider, String providerId, String email, String name, Long expiredMs) {
        System.out.println("임시 인증 토큰 생성 - 제공자: " + provider + ", providerId: " + providerId + ", email: " + email);
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
            System.out.println("임시 인증 토큰 생성 성공: " + token.substring(0, Math.min(token.length(), 10)) + "...");

            // 디버깅: 생성된 토큰 내용 확인
            try {
                var claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
                System.out.println("토큰에 저장된 이메일: " + claims.get("email", String.class));
            } catch (Exception e) {
                System.out.println("토큰 내용 확인 중 오류: " + e.getMessage());
            }

            return token;
        } catch (Exception e) {
            System.out.println("임시 인증 토큰 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public OAuthTempTokenDTO getTempTokenInfo(String token) {
        try {
            var claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

            String email = claims.get("email", String.class);
            System.out.println("토큰에서 추출한 이메일: " + email);

            return OAuthTempTokenDTO.builder()
                    .provider(claims.get("provider", String.class))
                    .providerId(claims.get("providerId", String.class))
                    .email(email)
                    .name(claims.get("name", String.class))
                    .registrationCompleted(claims.get("registrationCompleted", Boolean.class))
                    .build();
        } catch (Exception e) {
            System.out.println("임시 토큰 파싱 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Create a new refresh token for a user
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

    // Generate both access and refresh tokens
    @Transactional
    public TokenDTO generateTokenPair(PetUser user) {
        String accessToken;
        if (user.getEmail() != null) {
            accessToken = createJwtWithEmail(
                    user.getProvider(),
                    user.getSocialId(),
                    user.getUserId(),
                    user.getRole(),
                    user.getEmail(),
                    accessTokenExpirationMs);
        } else {
            accessToken = createJwt(
                    user.getProvider(),
                    user.getSocialId(),
                    user.getUserId(),
                    user.getRole(),
                    accessTokenExpirationMs);
        }

        String refreshToken = generateRefreshToken(user);

        return new TokenDTO(accessToken, refreshToken, accessTokenExpirationMs / 1000);
    }

    // Validate a refresh token and generate a new access token
    @Transactional
    public Optional<TokenDTO> refreshAccessToken(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .filter(RefreshToken::isValid)
                .map(token -> {
                    PetUser user = token.getUser();

                    // Create new access token with email
                    String newAccessToken;
                    if (user.getEmail() != null) {
                        newAccessToken = createJwtWithEmail(
                                user.getProvider(),
                                user.getSocialId(),
                                user.getUserId(),
                                user.getRole(),
                                user.getEmail(),
                                accessTokenExpirationMs);
                    } else {
                        newAccessToken = createJwt(
                                user.getProvider(),
                                user.getSocialId(),
                                user.getUserId(),
                                user.getRole(),
                                accessTokenExpirationMs);
                    }

                    // Return tokens
                    return new TokenDTO(newAccessToken, refreshToken, accessTokenExpirationMs / 1000);
                });
    }

    // Invalidate a refresh token
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

    // Revoke all refresh tokens for a user
    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUser(userId);
    }

    // Clean up expired tokens
    @Transactional
    public void removeExpiredTokens() {
        refreshTokenRepository.deleteAllExpiredTokens(LocalDateTime.now());
    }
}