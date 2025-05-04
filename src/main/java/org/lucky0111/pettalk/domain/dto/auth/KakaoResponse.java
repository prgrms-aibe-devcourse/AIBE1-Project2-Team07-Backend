package org.lucky0111.pettalk.domain.dto.auth;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 카카오 OAuth2 응답을 처리하는 클래스
 */
@Slf4j
public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attributes;
    private final Map<String, Object> properties;
    private final Map<String, Object> kakao_account;

    public KakaoResponse(Map<String, Object> attributes) {
        // attributes가 null이면 빈 맵으로 초기화
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();

        log.debug("Kakao 응답 처리 시작");

        // 안전하게 properties와 kakao_account 데이터 추출
        this.properties = extractMap(this.attributes, "properties");
        this.kakao_account = extractMap(this.attributes, "kakao_account");
    }

    /**
     * 맵에서 안전하게 하위 맵을 추출합니다.
     */
    private Map<String, Object> extractMap(Map<String, Object> source, String key) {
        if (source.containsKey(key) && source.get(key) instanceof Map) {
            Map<String, Object> result = new HashMap<>((Map<String, Object>) source.get(key));
            log.debug("Kakao {} 데이터 추출: {} 항목", key, result.size());
            return result;
        } else {
            log.debug("Kakao 응답에서 {} 데이터를 찾을 수 없습니다", key);
            return new HashMap<>();
        }
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return Optional.ofNullable(attributes.get("id"))
                .map(Object::toString)
                .orElseGet(() -> {
                    log.warn("Kakao 응답에서 ID를 찾을 수 없습니다");
                    return "unknown";
                });
    }

    @Override
    public String getEmail() {
        return Optional.ofNullable(kakao_account.get("email"))
                .map(Object::toString)
                .orElseGet(() -> {
                    log.debug("Kakao 응답에서 이메일을 찾을 수 없습니다");
                    return null;
                });
    }

    @Override
    public String getName() {
        // 1. properties에서 nickname 찾기
        String name = Optional.ofNullable(properties.get("nickname"))
                .map(Object::toString)
                .orElse(null);

        if (name != null) {
            return name;
        }

        // 2. kakao_account에서 profile의 nickname 찾기
        if (kakao_account.containsKey("profile") && kakao_account.get("profile") instanceof Map) {
            Map<String, Object> profile = (Map<String, Object>) kakao_account.get("profile");
            name = Optional.ofNullable(profile.get("nickname"))
                    .map(Object::toString)
                    .orElse(null);

            if (name != null) {
                return name;
            }
        }

        // 3. 대체 이름 생성
        String providerId = getProviderId();
        if ("unknown".equals(providerId)) {
            return "KakaoUser";
        }

        return "KakaoUser_" + providerId.substring(0, Math.min(providerId.length(), 6));
    }

    /**
     * 속성 맵을 반환합니다.
     */
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }

    /**
     * 프로퍼티 맵을 반환합니다.
     */
    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }

    /**
     * 카카오 계정 맵을 반환합니다.
     */
    public Map<String, Object> getKakaoAccount() {
        return new HashMap<>(kakao_account);
    }
}