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
    @Value("${spring.security.oauth2.redirect-url}")
    private String oAuth2RedirectUrl;

    private final JwtTokenProvider tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication authentication) throws IOException, ServletException {
        List<Cookie> cookies = tokenService.createTokenCookies(authentication);

        String redirectUrl = UriComponentsBuilder
                .fromUriString(oAuth2RedirectUrl)
                .queryParam("code", "register")
                .build()
                .toUriString();

        cookies.forEach(res::addCookie);
        res.sendRedirect(redirectUrl);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}