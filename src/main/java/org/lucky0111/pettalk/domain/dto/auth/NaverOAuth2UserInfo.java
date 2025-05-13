package org.lucky0111.pettalk.domain.dto.auth;

import org.lucky0111.pettalk.domain.common.OAuth2Provider;

import java.util.Map;

public class NaverOAuth2UserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;
    private final String accessToken;
    private final String id;
    private final String email;
    private final String name;

    public NaverOAuth2UserInfo(String registrationId, String accessToken, Map<String, Object> attributes) {
        Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");

        this.accessToken = accessToken;
        this.attributes = attributes;
        this.id = naverResponse.get("id").toString();
        this.email = naverResponse.get("email").toString();
        this.name = naverResponse.get("name").toString();
    }

    @Override
    public OAuth2Provider getProvider() {
        return OAuth2Provider.NAVER;
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
        return email;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNickname() {
        return null;
    }

    @Override
    public String getProfileImageUrl() {
        return null;
    }
}