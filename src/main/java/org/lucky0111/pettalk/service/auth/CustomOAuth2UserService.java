package org.lucky0111.pettalk.service.auth;


import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.dto.auth.KakaoResponse;
import org.lucky0111.pettalk.domain.dto.auth.NaverResponse;
import org.lucky0111.pettalk.domain.dto.auth.OAuth2Response;
import org.lucky0111.pettalk.domain.dto.user.UserDTO;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final PetUserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oAuth2User = super.loadUser(userRequest);
            System.out.println("OAuth2 로그인 사용자 정보: " + oAuth2User);

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            OAuth2Response oAuth2Response = null;

            // 안전하게 속성 복사
            Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

            if (registrationId.equals("naver")) {
                oAuth2Response = new NaverResponse(attributes);
                System.out.println("네이버 로그인 처리");
            }
            else if (registrationId.equals("kakao")) {
                oAuth2Response = new KakaoResponse(attributes);
                System.out.println("카카오 로그인 처리");
            }
            else {
                System.out.println("지원하지 않는 OAuth2 제공자: " + registrationId);
                throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 제공자입니다.");
            }

            // OAuth2 응답 정보 확인
            if (oAuth2Response == null) {
                System.out.println("OAuth2 응답 생성 실패");
                throw new OAuth2AuthenticationException("OAuth2 응답 정보를 가져올 수 없습니다.");
            }

            String provider = oAuth2Response.getProvider();
            String socialId = oAuth2Response.getProviderId();
            String email = oAuth2Response.getEmail();
            String name = oAuth2Response.getName();

            System.out.println("OAuth2 사용자 정보: provider=" + provider +
                    ", socialId=" + socialId + ", email=" + email + ", name=" + name);

            if (provider == null || socialId == null) {
                System.out.println("필수 정보 누락");
                throw new OAuth2AuthenticationException("OAuth2 응답에 필수 정보가 누락되었습니다.");
            }

            PetUser existData = userRepository.findByProviderAndSocialId(provider, socialId);

            UserDTO userDTO;

            if (existData == null) {
                System.out.println("신규 사용자 - 첫 로그인");
                userDTO = new UserDTO("ROLE_USER", name, provider, socialId, null, email);
            }
            else {
                System.out.println("기존 사용자 정보: " + existData.getUserId() + ", " + existData.getRole());
                userDTO = new UserDTO(existData.getRole(), existData.getName(), provider, socialId, existData.getUserId(), existData.getEmail());
            }

            // 이메일 정보를 포함하여 CustomOAuth2User 생성
            return new CustomOAuth2User(userDTO, attributes);

        } catch (Exception e) {
            System.err.println("OAuth2 사용자 로드 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new OAuth2AuthenticationException("OAuth2 인증 처리 중 오류가 발생했습니다.");
        }
    }
}