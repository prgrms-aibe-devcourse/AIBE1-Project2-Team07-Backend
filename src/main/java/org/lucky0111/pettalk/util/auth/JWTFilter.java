package org.lucky0111.pettalk.util.auth;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.dto.auth.TokenDTO;
import org.lucky0111.pettalk.domain.dto.user.UserDTO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    // 필터를 적용하지 않을 경로 목록
    private final List<String> excludedPaths = Arrays.asList(
            "/api/v1/auth/refresh",
            "/api/v1/auth/check-nickname",
            "/api/v1/auth/register",
            "/",
            "/swagger-ui/",
            "/v3/api-docs/"
    );

    // 토큰 자동 갱신을 위한 임계값 (초)
    private static final long TOKEN_REFRESH_THRESHOLD_SECONDS = 5 * 60; // 5분

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Skip filter for excluded paths
        String path = request.getRequestURI();

        if (shouldSkipFilter(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = extractToken(request);

        //Authorization 헤더 검증
        if (accessToken == null) {
            log.debug("토큰이 없습니다");
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 만료 확인
        if (jwtUtil.isExpired(accessToken)) {
            log.debug("토큰이 만료되었습니다");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Access token expired\",\"code\":\"TOKEN_EXPIRED\"}");
            return;
        }

        // 토큰 갱신이 필요한지 확인 (만료 5분 전)
        long expiresIn = jwtUtil.getExpiresIn(accessToken);
        if (expiresIn <= TOKEN_REFRESH_THRESHOLD_SECONDS) {
            log.debug("토큰 갱신이 필요합니다. 남은 시간: {}초", expiresIn);

            // 리프레시 토큰 확인 (요청 헤더에서)
            String refreshToken = extractRefreshToken(request);
            if (refreshToken != null) {
                Optional<TokenDTO> newTokens = jwtUtil.refreshAccessToken(refreshToken);
                if (newTokens.isPresent()) {
                    // 새 액세스 토큰으로 교체
                    accessToken = newTokens.get().accessToken();
                    // 응답 헤더에 새 토큰 추가
                    response.setHeader("New-Access-Token", accessToken);
                    response.setHeader("New-Refresh-Token", newTokens.get().refreshToken());
                    response.setHeader("Token-Expires-In", String.valueOf(newTokens.get().expiresIn()));
                    log.debug("토큰이 자동으로 갱신되었습니다.");
                }
            }
        }

        // 토큰에서
        String provider = jwtUtil.getProvider(accessToken);
        String socialId = jwtUtil.getSocialId(accessToken);
        String role = jwtUtil.getRole(accessToken);
        UUID userId = jwtUtil.getUserId(accessToken);
        String email = jwtUtil.getEmail(accessToken);

        //userDTO를 생성하여 값 set
        UserDTO userDTO = new UserDTO(role, null, provider, socialId, userId, email);

        //UserDetails에 회원 정보 객체 담기
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDTO);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

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