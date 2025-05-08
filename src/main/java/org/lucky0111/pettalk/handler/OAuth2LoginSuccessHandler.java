package org.lucky0111.pettalk.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.service.auth.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    @Value("${front-url}")
    private String frontUrl;

    private final JwtTokenProvider tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication authentication) throws IOException, ServletException {
        List<Cookie> cookies = tokenService.createTokenCookies(authentication);


        String accessToken = tokenService.createAccessToken(authentication);
        String refreshToken = tokenService.createRefreshToken(authentication);

        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontUrl + "auth/oauth2/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();
//
//        for (Cookie cookie : cookies) {
//            String cookieHeader = String.format("%s=%s; Path=%s; Max-Age=%d; HttpOnly; Secure; SameSite=None",
//                    cookie.getName(),
//                    cookie.getValue(),
//                    cookie.getPath(),
//                    cookie.getMaxAge());
//
//            res.addHeader("Set-Cookie", cookieHeader);
//        }

        // CORS 관련 헤더 추가
//        res.addHeader("Access-Control-Allow-Origin", frontUrl);
//        res.addHeader("Access-Control-Allow-Credentials", "true");

        res.sendRedirect(redirectUrl);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}