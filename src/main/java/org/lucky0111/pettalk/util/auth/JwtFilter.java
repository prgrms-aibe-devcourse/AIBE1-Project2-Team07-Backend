package org.lucky0111.pettalk.util.auth;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.common.TokenStatus;
import org.lucky0111.pettalk.service.auth.JwtTokenProvider;
import org.lucky0111.pettalk.service.user.UserService;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // TODO: 필터 구현 필요
        String accessToken = request.getHeader("Authorization");

        if (accessToken == null || accessToken.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        TokenStatus tokenStatus = tokenProvider.validateToken(accessToken);

        switch (tokenStatus) {
            case AUTHENTICATED -> {
                log.info("Access Token Validation Success");
                
            }

            case EXPIRED -> {
                log.info("Access Token Expired");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }

            case INVALIDATED -> {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludePath = {"/swagger-ui/**", "/v3/api-docs/**", "/api/v1/auth/**"};
        String path = request.getRequestURI();
        AntPathMatcher pathMatcher = new AntPathMatcher();

        return Arrays.stream(excludePath)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}