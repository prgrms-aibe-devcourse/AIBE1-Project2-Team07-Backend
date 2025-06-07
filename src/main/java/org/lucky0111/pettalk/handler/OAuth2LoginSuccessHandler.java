package org.lucky0111.pettalk.handler;

import jakarta.servlet.ServletException;
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

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    @Value("${front-url}")
    private String frontUrl;

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            String accessToken = jwtTokenProvider.createAccessToken(authentication);
            String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
            long accessTokenExpiresIn = jwtTokenProvider.getExpiresInSeconds(accessToken);
            long refreshTokenExpiresIn = jwtTokenProvider.getExpiresInSeconds(refreshToken);
            jwtTokenProvider.saveRefreshToken(accessToken);

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
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("OAuth2 로그인 처리 중 오류가 발생했습니다.");
            res.getWriter().flush();
            res.getWriter().close();
        }
    }
}