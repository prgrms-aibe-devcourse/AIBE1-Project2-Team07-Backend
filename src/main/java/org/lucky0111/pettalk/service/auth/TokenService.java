package org.lucky0111.pettalk.service.auth;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.common.TokenStatus;
import org.lucky0111.pettalk.domain.common.TokenType;
import org.lucky0111.pettalk.domain.dto.auth.OAuth2UserInfo;
import org.lucky0111.pettalk.util.auth.JWTUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JWTUtil jwtUtil;

    public List<Cookie> reissue(Authentication authentication) {
        List<Cookie> cookies = List.of();
        OAuth2UserInfo oAuth2UserInfo = (OAuth2UserInfo) authentication.getPrincipal();
        String username = oAuth2UserInfo.getName();
        Collection<? extends GrantedAuthority> authorities = ((OAuth2User) authentication.getPrincipal()).getAuthorities();
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, "", authorities);
        List<String> roles = authToken.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String accessToken = jwtUtil.createJwt(TokenType.ACCESS, UUID.fromString(username), roles);
        String refreshToken = jwtUtil.createJwt(TokenType.REFRESH, UUID.fromString(username), roles);
        cookies = List.of(
                new Cookie(TokenType.ACCESS.getName(), accessToken),
                new Cookie(TokenType.REFRESH.getName(), refreshToken)
        );
        return cookies;
    }

    public String getAccessTokenFromCookie(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie
                        .getName().equals(TokenType.ACCESS.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    public String getRefreshTokenFromCookie(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie
                        .getName().equals(TokenType.REFRESH.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    public TokenStatus validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}
