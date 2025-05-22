package org.lucky0111.pettalk.service.auth;


import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.common.OAuth2Provider;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.dto.auth.OAuth2UserInfo;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.lucky0111.pettalk.util.auth.OAuth2UserInfoFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final OAuth2UserInfoFactory oAuth2UserInfoFactory;
    private final PetUserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(userRequest, oAuth2User);

        String socialId = oAuth2UserInfo.getId();
        String email = oAuth2UserInfo.getEmail();

        PetUser user = findOrSaveUser(socialId, email, oAuth2UserInfo.getProvider());
        return new CustomOAuth2User(user.getUserId().toString(), oAuth2UserInfo, List.of(new SimpleGrantedAuthority(user.getRole().toString())));
    }

    private OAuth2UserInfo getOAuth2UserInfo(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        return oAuth2UserInfoFactory.getOAuth2UserInfo(userRequest.getClientRegistration().getRegistrationId(),
                userRequest.getAccessToken().getTokenValue(),
                oAuth2User.getAttributes());
    }

    private PetUser findOrSaveUser(String username, String email, OAuth2Provider oAuth2Provider) {
        // TODO: 유저 저장 기준을 이메일로 하고 있음. socialId로 변경 시 소셜 서비스별 계정 생성 가능
        return userRepository.findBySocialId(username)
                .orElseGet(() -> createUser(username, email, oAuth2Provider));
    }

    private PetUser createUser(String socialId, String email, OAuth2Provider oAuth2Provider) {
        PetUser user = PetUser.builder()
                .name("임시사용자")
                .email(email)
                .nickname("임시사용자_" + UUID.randomUUID())
                .profileImageUrl("https://pet-talk-bucket.s3.ap-northeast-2.amazonaws.com/default.png")
                .provider(oAuth2Provider.getRegistrationId())
                .socialId(socialId)
                .build();

        return userRepository.save(user);
    }
}