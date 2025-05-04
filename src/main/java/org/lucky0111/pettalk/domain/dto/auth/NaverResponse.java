package org.lucky0111.pettalk.domain.dto.auth;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 네이버 OAuth2 응답을 처리하는 클래스
 */
@Slf4j
public class NaverResponse implements OAuth2Response {

    private final Map<String, Object> responseData;
    private final Map<String, Object> originalAttributes;

    public NaverResponse(Map<String, Object> attributes) {
        // attributes가 null이면 빈 맵으로 초기화
        this.originalAttributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();

        log.debug("Naver 응답 처리 시작");

        // 안전하게 response 데이터 추출
        if (this.originalAttributes.containsKey("response") &&
                this.originalAttributes.get("response") instanceof Map) {
            // response 맵을 복사하여 새 맵 생성 (불변 맵 문제 방지)
            this.responseData = new HashMap<>((Map<String, Object>) this.originalAttributes.get("response"));
            log.debug("Naver response 데이터 추출: {} 항목", this.responseData.size());
        } else {
            // Naver의 응답 형식이 예상과 다른 경우 처리
            log.warn("예상과 다른 네이버 응답 형식. 원본 속성을 사용합니다.");
            this.responseData = this.originalAttributes;
        }
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        // 1. responseData에서 id 찾기
        String id = Optional.ofNullable(responseData.get("id"))
                .map(Object::toString)
                .orElse(null);

        if (id != null) {
            return id;
        }

        // 2. originalAttributes에서 id 찾기
        id = Optional.ofNullable(originalAttributes.get("id"))
                .map(Object::toString)
                .orElse(null);

        if (id != null) {
            return id;
        }

        // 3. id를 찾지 못한 경우
        log.warn("Naver 응답에서 ID를 찾을 수 없습니다");
        return "unknown";
    }

    @Override
    public String getEmail() {
        // 1. responseData에서 email 찾기
        String email = Optional.ofNullable(responseData.get("email"))
                .map(Object::toString)
                .orElse(null);

        if (email != null) {
            return email;
        }

        // 2. originalAttributes에서 email 찾기
        return Optional.ofNullable(originalAttributes.get("email"))
                .map(Object::toString)
                .orElseGet(() -> {
                    log.debug("Naver 응답에서 이메일을 찾을 수 없습니다");
                    return null;
                });
    }

    @Override
    public String getName() {
        // 1. responseData에서 name 찾기
        String name = Optional.ofNullable(responseData.get("name"))
                .map(Object::toString)
                .orElse(null);

        if (name != null) {
            return name;
        }

        // 2. originalAttributes에서 name 찾기
        name = Optional.ofNullable(originalAttributes.get("name"))
                .map(Object::toString)
                .orElse(null);

        if (name != null) {
            return name;
        }

        // 3. 대체 이름 생성
        String providerId = getProviderId();
        if ("unknown".equals(providerId)) {
            return "NaverUser";
        }

        return "NaverUser_" + providerId.substring(0, Math.min(providerId.length(), 6));
    }

    /**
     * 응답 데이터 맵을 반환합니다.
     */
    public Map<String, Object> getResponseData() {
        return new HashMap<>(responseData);
    }

    /**
     * 원본 속성 맵을 반환합니다.
     */
    public Map<String, Object> getOriginalAttributes() {
        return new HashMap<>(originalAttributes);
    }
}