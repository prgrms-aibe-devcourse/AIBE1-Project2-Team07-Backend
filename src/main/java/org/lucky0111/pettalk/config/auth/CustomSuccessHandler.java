package org.lucky0111.pettalk.config.auth;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.dto.auth.OAuth2Response;

import org.lucky0111.pettalk.domain.dto.auth.TokenDTO;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.lucky0111.pettalk.util.auth.JWTUtil;
import org.lucky0111.pettalk.util.auth.OAuth2UserServiceHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final OAuth2UserServiceHelper oAuth2UserServiceHelper;
    private final PetUserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Value("${front.url}")
    private String frontUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 로그인 성공! 인증 정보: {}", authentication);

        // OAuth2User 정보 추출
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = authToken.getPrincipal();

        String registrationId = authToken.getAuthorizedClientRegistrationId();
        String provider = null;
        String providerId = null;
        String email = null;
        String name = null;

        // CustomOAuth2User인 경우
        if (oAuth2User instanceof CustomOAuth2User) {
            CustomOAuth2User customUser = (CustomOAuth2User) oAuth2User;

            provider = customUser.getProvider();
            providerId = customUser.getSocialId();
            email = customUser.getEmail(); // getEmail() 메서드 사용
            if (email == null) {
                // attributes에서 이메일 검색 시도
                if (customUser.getAttributes().containsKey("email")) {
                    email = (String) customUser.getAttributes().get("email");
                }
            }
            name = customUser.getName();

            log.debug("CustomOAuth2User에서 정보 추출: provider={}, providerId={}, email={}, name={}",
                    provider, providerId, email, name);

            // 이미 가입한 사용자인 경우 바로 로그인
            PetUser existingUser = userRepository.findByProviderAndSocialId(provider, providerId);
            if (existingUser != null && existingUser.getNickname() != null) {
                redirectWithTokens(response, existingUser);
                return;
            }
        }
        // 일반 OAuth2User인 경우 - OAuth2UserServiceHelper 사용
        else {
            // 안전하게 맵 복사
            Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

            // OAuth2 응답 정보 처리
            OAuth2Response oAuth2Response = oAuth2UserServiceHelper.getOAuth2Response(registrationId, attributes);

            if (oAuth2Response == null) {
                log.error("OAuth2 응답 생성 실패: 제공자 = {}", registrationId);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원하지 않는 OAuth2 제공자입니다.");
                return;
            }

            provider = oAuth2Response.getProvider();
            providerId = oAuth2Response.getProviderId();
            email = oAuth2Response.getEmail();
            name = oAuth2Response.getName();

            log.debug("OAuth2 사용자 정보: provider={}, id={}, email={}, name={}",
                    provider, providerId, email, name);

            // 이미 가입한 사용자인 경우 바로 로그인
            PetUser existingUser = userRepository.findByProviderAndSocialId(provider, providerId);
            if (existingUser != null && existingUser.getNickname() != null) {
                redirectWithTokens(response, existingUser);
                return;
            }
        }

        // provider나 providerId가 null이면 오류 반환
        if (provider == null || providerId == null) {
            log.error("OAuth2 응답에 필수 정보가 누락되었습니다.");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "OAuth2 응답에 필수 정보가 누락되었습니다.");
            return;
        }

        // 임시 토큰 생성 (새 사용자 또는 추가 정보가 필요한 사용자)
        String tempToken = jwtUtil.createTempToken(
                provider,
                providerId,
                email,
                name,
                30 * 60 * 1000L // 30분
        );

        // 프론트엔드로 리다이렉트 (임시 토큰 포함 - 이 경우에는 URL에 토큰 필요)
        String targetUrl = frontUrl + "/register?token=" + tempToken;
        log.debug("리다이렉트 주소: {}", targetUrl);
        response.sendRedirect(targetUrl);
    }

    /**
     * 사용자 인증 성공 후 토큰을 생성하고 프론트엔드로 리다이렉트합니다.
     */
    private void redirectWithTokens(HttpServletResponse response, PetUser user) throws IOException {
        // 토큰 생성
        TokenDTO tokens = jwtUtil.generateTokenPair(user);

        // 응답 데이터 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("accessToken", tokens.accessToken());
        responseData.put("refreshToken", tokens.refreshToken());
        responseData.put("expiresIn", tokens.expiresIn());

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getUserId());
        userData.put("email", user.getEmail());
        userData.put("name", user.getName());
        userData.put("nickname", user.getNickname());
        userData.put("profileImageUrl", user.getProfileImageUrl());
        userData.put("role", user.getRole());

        responseData.put("user", userData);

        // 데이터를 JSON 문자열로 변환
        String jsonData = objectMapper.writeValueAsString(responseData);

        // JSON 데이터를 Base64로 인코딩
        String encodedData = Base64.getEncoder().encodeToString(jsonData.getBytes(StandardCharsets.UTF_8));

        // URL에 인코딩된 데이터 추가
        String targetUrl = frontUrl + "/oauth/callback?data=" + URLEncoder.encode(encodedData, StandardCharsets.UTF_8);

        response.sendRedirect(targetUrl);
    }
}