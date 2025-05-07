package org.lucky0111.pettalk.controller.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.common.TokenStatus;
import org.lucky0111.pettalk.service.auth.TokenService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
public class TokenController {
    private final TokenService tokenService;

    @PostMapping("/reissue")
    public void reissueToken(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Cookie[] cookies = request.getCookies();

        String refreshToken = tokenService.getRefreshTokenFromCookie(cookies);

        TokenStatus tokenStatus = tokenService.validateToken(refreshToken);

        switch (tokenStatus) {
            case INVALIDATED -> response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            case EXPIRED -> {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            case AUTHENTICATED -> {
                List<Cookie> newCookies = tokenService.reissue(authentication);
                newCookies.forEach(response::addCookie);
                response.setStatus(HttpServletResponse.SC_OK);
            }
        }
    }
}
