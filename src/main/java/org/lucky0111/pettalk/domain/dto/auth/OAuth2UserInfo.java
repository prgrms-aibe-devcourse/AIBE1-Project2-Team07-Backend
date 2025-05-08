package org.lucky0111.pettalk.domain.dto.auth;


import org.lucky0111.pettalk.domain.common.OAuth2Provider;

import java.util.Map;

public interface OAuth2UserInfo {

    OAuth2Provider getProvider();

    String getAccessToken();

    Map<String, Object> getAttributes();

    String getId();

    String getEmail();

    String getName();

    String getNickname();

    String getProfileImageUrl();
}