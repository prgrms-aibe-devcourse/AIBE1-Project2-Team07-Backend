package org.lucky0111.pettalk.service.auth;


import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.common.OAuth2Provider;
import org.lucky0111.pettalk.domain.dto.auth.OAuth2UserInfo;
import org.lucky0111.pettalk.domain.dto.auth.OAuth2UserPrincipal;
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
        String username = oAuth2UserInfo.getId();
        PetUser user = findOrSaveUser(username, oAuth2UserInfo.getProvider());
        return new OAuth2UserPrincipal(user.getUserId().toString(), oAuth2UserInfo, List.of(new SimpleGrantedAuthority(user.getRole())));
    }

    private OAuth2UserInfo getOAuth2UserInfo(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        return oAuth2UserInfoFactory.getOAuth2UserInfo(userRequest.getClientRegistration().getRegistrationId(),
                userRequest.getAccessToken().getTokenValue(),
                oAuth2User.getAttributes());
    }

    private PetUser findOrSaveUser(String username, OAuth2Provider oAuth2Provider) {
        return userRepository.findById(UUID.fromString(username))
                .orElseGet(() -> userRepository.save(
                        PetUser.of(username,
                                Role.ROLE_USER,
                                Platform.valueOf(oAuth2Provider.toString().toUpperCase()))
                )));
    }
}