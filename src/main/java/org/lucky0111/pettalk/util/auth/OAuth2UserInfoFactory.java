package org.lucky0111.pettalk.util.auth;

import org.lucky0111.pettalk.domain.dto.auth.OAuth2UserInfo;

import java.util.Map;

public interface OAuth2UserInfoFactory {
    OAuth2UserInfo getOAuth2UserInfo(String registrationId,
                                     String accessToken,
                                     Map<String, Object> attributes);
}

