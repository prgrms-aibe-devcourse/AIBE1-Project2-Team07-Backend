package org.lucky0111.pettalk.service.auth;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.lucky0111.pettalk.domain.common.TokenStatus;
import org.lucky0111.pettalk.domain.common.TokenType;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.entity.auth.RefreshToken;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.repository.auth.RefreshTokenRepository;
import org.lucky0111.pettalk.service.user.UserService;
import org.lucky0111.pettalk.util.auth.JwtUtil;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    @Transactional
    public List<Cookie> createTokenCookies(Authentication authentication) {
        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
        UUID userId = UUID.fromString(oauthUser.getName());

        List<String> roles = extractRoles(authentication);
        String accessToken = jwtUtil.createJwt(TokenType.ACCESS, userId, roles);
        String refreshToken = jwtUtil.createJwt(TokenType.REFRESH, userId, roles);

        PetUser petUser = userService.getUserById(userId);
        updateRefreshToken(petUser, refreshToken);

        return List.of(
                createTokenCookie(TokenType.ACCESS, accessToken),
                createTokenCookie(TokenType.REFRESH, refreshToken)
        );
    }

    @Transactional
    public String reissue(PetUser petUser, String refreshToken) throws BadRequestException {
        TokenStatus status = validateToken(refreshToken);

        if (status == TokenStatus.AUTHENTICATED) {
            updateRefreshToken(petUser, refreshToken);
            return jwtUtil.createJwt(TokenType.ACCESS, getUserId(refreshToken), getRoles(refreshToken));
        }

        if (status == TokenStatus.EXPIRED) {
            throw new CredentialsExpiredException("리프레시 토큰 만료. 로그인을 다시 해주세요.");
        }

        throw new BadRequestException(status.getDescription());
    }

    private void updateRefreshToken(PetUser user, String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(new RefreshToken());

        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(getTokenExpiryDate(token));

        refreshTokenRepository.save(refreshToken);
    }


    private List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    private Cookie createTokenCookie(TokenType type, String token) {
        Cookie cookie = new Cookie(type.getName(), token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) jwtUtil.getExpiresIn(token));
        // cookie.setSecure(true); // TODO: prod로 배포할 때 활성화
        return cookie;
    }

    private LocalDateTime getTokenExpiryDate(String token) {
        Instant expiryInstant = Instant.ofEpochSecond(jwtUtil.getExpiresIn(token));
        return LocalDateTime.ofInstant(expiryInstant, ZoneId.of("UTC"));
    }

    public String extractTokenFromCookies(Cookie[] cookies, TokenType type) {
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(cookie -> type.getName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public TokenStatus validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public UUID getUserId(String token) {
        return jwtUtil.getUserId(token);
    }

    public List<String> getRoles(String token) {
        return jwtUtil.getRoles(token);
    }
}
