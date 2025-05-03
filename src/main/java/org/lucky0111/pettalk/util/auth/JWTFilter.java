package org.lucky0111.pettalk.util.auth;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.dto.user.UserDTO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    // 필터를 적용하지 않을 경로 목록
    private final List<String> excludedPaths = Arrays.asList(
            "/api/v1/auth/refresh",
            "/api/v1/auth/check-nickname",
            "/api/v1/auth/register",
            "/",
            "/swagger-ui/",
            "/v3/api-docs/",
            "/swagger-resources/",
            "/swagger-ui.html",
            "/webjars/",
            "/api-docs/"
    );

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Skip filter for excluded paths
        logger.info("doFilterStart");
        String path = request.getRequestURI();

        if (shouldSkipFilter(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = extractToken(request);

        //Authorization 헤더 검증
        if (authorization == null) {
            System.out.println("token null");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        //토큰
        String token = authorization;

        if (jwtUtil.isExpired(token)) {
            System.out.println("token expired");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Access token expired\",\"code\":\"TOKEN_EXPIRED\"}");
            return;
        }

        //토큰에서 username과 role 획득
        String provider = jwtUtil.getProvider(token);
        String socialId = jwtUtil.getSocialId(token);
        String role = jwtUtil.getRole(token);
        UUID userId = jwtUtil.getUserId(token);
        String email = jwtUtil.getEmail(token);

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

        // If not in header, check cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("Authorization")) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}