package org.lucky0111.pettalk.util.auth;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.common.TokenStatus;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.service.auth.JwtTokenProvider;
import org.springframework.http.HttpMethod;
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
    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String accessToken = header.substring(7);

        // GET 메서드는 필터 통과..
        if (request.getMethod().equals(HttpMethod.GET.name())) {
            SecurityContextHolder.getContext().setAuthentication(createAuthenticationToken(accessToken));
            filterChain.doFilter(request, response);
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
                return;
            }

            case INVALIDATED -> {
                log.info("Access Token Invalidated");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludePath = {"/swagger-ui/**", "/v3/api-docs/**"};
        String path = request.getRequestURI();
        AntPathMatcher pathMatcher = new AntPathMatcher();

        return Arrays.stream(excludePath)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}