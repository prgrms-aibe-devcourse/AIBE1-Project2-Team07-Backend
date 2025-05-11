package org.lucky0111.pettalk.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.service.auth.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    @Value("${front-url}")
    private String frontUrl;

    private final JwtTokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication authentication) throws IOException, ServletException {
        String accessToken = tokenService.createAccessToken(authentication);
        String refreshToken = tokenService.createRefreshToken(authentication);
        long accessTokenExpiresIn = tokenService.getExpiresInSeconds(accessToken);
        long refreshTokenExpiresIn = tokenService.getExpiresInSeconds(refreshToken);
        tokenService.saveRefreshToken(accessToken);

        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontUrl + "auth/oauth2/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("accessTokenExpiresIn", accessTokenExpiresIn)
                .queryParam("refreshTokenExpiresIn", refreshTokenExpiresIn)
                .build()
                .toUriString();

        res.sendRedirect(redirectUrl);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}