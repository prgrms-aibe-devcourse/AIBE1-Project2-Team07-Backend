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
}