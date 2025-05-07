package org.lucky0111.pettalk.domain.dto.auth;

import org.lucky0111.pettalk.domain.common.OAuth2Provider;

import java.util.Map;

public class NaverOAuth2UserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;
    private final String accessToken;
    private final String id;
    private final String nickName;

    public NaverOAuth2UserInfo(String registrationId, String accessToken, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        this.id = registrationId + "_" + attributes.get("id").toString();
        this.accessToken = accessToken;
        this.attributes = attributes;
        this.nickName = profile.get("nickname").toString();
    }

    @Override
    public OAuth2Provider getProvider() {
        return OAuth2Provider.KAKAO;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getNickname() {
        return nickName;
    }

    @Override
    public String getProfileImageUrl() {
        return null;
    }
}