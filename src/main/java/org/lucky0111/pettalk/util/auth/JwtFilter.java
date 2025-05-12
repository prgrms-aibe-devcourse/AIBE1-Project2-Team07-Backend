package org.lucky0111.pettalk.util.auth;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.common.TokenStatus;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.dto.error.ErrorResponseDTO;
import org.lucky0111.pettalk.service.auth.JwtTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtTokenService tokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludePath = {"/swagger-ui/**", "/v3/api-docs/**", "/api/v1/auth/**"};
        String path = request.getRequestURI();
        AntPathMatcher pathMatcher = new AntPathMatcher();

        return Arrays.stream(excludePath)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String path = request.getRequestURI();

        if (header == null || !header.startsWith("Bearer ")) {
            if (path.contains("/open")){
                filterChain.doFilter(request, response);
                return;
            }
            createErrorResponse(response, HttpStatus.FORBIDDEN, "엑세스 토큰이 없습니다.");
            return;
        }

        String accessToken = header.substring(7);

        TokenStatus tokenStatus = tokenProvider.validateToken(accessToken);

        switch (tokenStatus) {
            case AUTHENTICATED -> {
                log.info("Access Token Validation Success");
            }

            case EXPIRED -> {
                log.info("Access Token Expired");
                createErrorResponse(response, HttpStatus.UNAUTHORIZED, "엑세스 토큰 만료. 로그인을 다시 해주세요.");
                return;
            }

            case INVALIDATED -> {
                log.info("Access Token Invalidated");
                createErrorResponse(response, HttpStatus.FORBIDDEN, "엑세스 토큰이 유효하지 않습니다.");
                return;
            }
        }

        SecurityContextHolder.getContext().setAuthentication(createAuthenticationToken(accessToken));
        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken createAuthenticationToken(String accessToken) {
        UUID userId = tokenProvider.getUserId(accessToken);
        List<String> roles = tokenProvider.getRoles(accessToken);
        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userId.toString(), null, authorities);
        return new UsernamePasswordAuthenticationToken(customOAuth2User, null, authorities);
    }

    private void createErrorResponse(HttpServletResponse response, HttpStatus httpStatus, String message) throws IOException {
        response.setStatus(httpStatus.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(createErrorResponse(httpStatus, message).toString());
    }

    private ErrorResponseDTO createErrorResponse(HttpStatus httpStatus, String message) {
        return ErrorResponseDTO.builder()
                .status(httpStatus.value())
                .message(message)
                .build();
    }
}