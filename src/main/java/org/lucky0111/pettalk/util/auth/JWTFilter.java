package org.lucky0111.pettalk.util.auth;


import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.common.TokenStatus;
import org.lucky0111.pettalk.service.auth.TokenService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = tokenService.getAccessTokenFromCookie(request.getCookies());

        if (accessToken == null || accessToken.isEmpty()) {
            TokenStatus tokenStatus = tokenService.validateToken(accessToken);

            switch (tokenStatus) {
                case INVALIDATED -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                case EXPIRED -> {
                    RequestDispatcher dispatcher = request.getRequestDispatcher("/api/v1/token/reissue");
                    dispatcher.forward(request, response);
                }
                case AUTHENTICATED -> response.setStatus(HttpServletResponse.SC_OK);
            }
        }

        filterChain.doFilter(request, response);
    }

    // 요청 경로가 제외 목록의 경로 패턴과 일치하는지 확인
    private boolean shouldSkipFilter(String requestPath) {
        return false;
//        return excludedPaths.stream()
//                .anyMatch(path -> {
//                    if (path.endsWith("/")) {
//                        // 경로가 '/'로 끝나면 하위 경로를 포함하는 패턴 매칭
//                        return requestPath.startsWith(path);
//                    } else {
//                        // 정확한 경로 일치 확인
//                        return requestPath.equals(path);
//                    }
//                });
    }

    private String extractToken(HttpServletRequest request) {
        // Try to get token from Authorization header first
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    private String extractRefreshToken(HttpServletRequest request) {
        // 리프레시 토큰은 별도의 헤더에서 확인
        return request.getHeader("Refresh-Token");
    }
}