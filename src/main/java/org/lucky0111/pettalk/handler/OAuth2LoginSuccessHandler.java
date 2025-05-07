package org.lucky0111.pettalk.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.common.TokenType;
import org.lucky0111.pettalk.util.auth.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    @Value("${spring.security.oauth2.redirect-url}")
    private String oAuth2RedirectUrl;

    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String username = oAuth2User.getName();

        Collection<? extends GrantedAuthority> authorities = ((OAuth2User) authentication.getPrincipal()).getAuthorities();
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, "", authorities);
        List<String> roles = authToken.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String accessToken = jwtUtil.createJwt(TokenType.ACCESS, UUID.fromString(username), roles);

        String redirectUrl = UriComponentsBuilder
                .fromUriString(oAuth2RedirectUrl)
                .queryParam("token", accessToken)
                .build().toUriString();

        res.sendRedirect(redirectUrl);
    }
}