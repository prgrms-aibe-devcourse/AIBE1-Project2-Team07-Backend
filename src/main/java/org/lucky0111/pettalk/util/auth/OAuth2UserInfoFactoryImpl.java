package org.lucky0111.pettalk.util.auth;

import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.common.OAuth2Provider;
import org.lucky0111.pettalk.domain.dto.auth.KakaoOAuth2UserInfo;
import org.lucky0111.pettalk.domain.dto.auth.NaverOAuth2UserInfo;
import org.lucky0111.pettalk.domain.dto.auth.OAuth2UserInfo;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2UserInfoFactoryImpl implements OAuth2UserInfoFactory {
    @Override
    public OAuth2UserInfo getOAuth2UserInfo(String registrationId, String accessToken, Map<String, Object> attributes) {
        OAuth2Provider provider = OAuth2Provider.valueOf(registrationId.toUpperCase());

        return switch (provider) {
            case KAKAO -> new KakaoOAuth2UserInfo(provider.getRegistrationId(), accessToken, attributes);
            case NAVER -> new NaverOAuth2UserInfo(provider.getRegistrationId(), accessToken, attributes);

            default -> {
                throw new IllegalArgumentException("Unsupported OAuth2 provider: " + provider);
            }
        };
    }
}
