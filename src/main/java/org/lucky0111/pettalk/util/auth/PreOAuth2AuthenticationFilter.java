package org.lucky0111.pettalk.util.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.common.TokenStatus;
import org.lucky0111.pettalk.domain.common.TokenType;
import org.lucky0111.pettalk.service.auth.JwtTokenProvider;
import org.lucky0111.pettalk.service.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

//@Component
@RequiredArgsConstructor
public class PreOAuth2AuthenticationFilter extends OncePerRequestFilter {
    @Value("${spring.security.oauth2.redirect-url}")
    private String oAuth2RedirectUrl;

    private final JwtTokenProvider tokenService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();
        String refreshToken = tokenService.extractTokenFromCookies(cookies, TokenType.REFRESH);
        TokenStatus tokenStatus = tokenService.validateToken(refreshToken);

        if (tokenStatus == TokenStatus.AUTHENTICATED) {
            UUID userId = tokenService.getUserId(refreshToken);

            if (!userService.isGuest(userId)) {
                String redirectUrl = UriComponentsBuilder
                        .fromUriString(oAuth2RedirectUrl)
                        .build()
                        .toUriString();

                response.sendRedirect(redirectUrl);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludePath = {
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/api/v1/auth/**"};
        String path = request.getRequestURI();
        AntPathMatcher pathMatcher = new AntPathMatcher();

        return Arrays.stream(excludePath)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
