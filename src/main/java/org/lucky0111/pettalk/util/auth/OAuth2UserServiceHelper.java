package org.lucky0111.pettalk.util.auth;

import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.dto.auth.KakaoResponse;
import org.lucky0111.pettalk.domain.dto.auth.NaverResponse;
import org.lucky0111.pettalk.domain.dto.auth.OAuth2Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 사용자 서비스를 위한 헬퍼 클래스
 */
@Slf4j
@Component
public class OAuth2UserServiceHelper {

    /**
     * OAuth2 응답 정보를 생성합니다.
     *
     * @param registrationId OAuth2 등록 ID (제공자)
     * @param attributes     OAuth2 속성 맵
     * @return OAuth2Response 객체
     */
    public OAuth2Response getOAuth2Response(String registrationId, Map<String, Object> attributes) {
        // attributes가 null인 경우 빈 맵으로 초기화
        Map<String, Object> safeAttributes;
        if (attributes == null) {
            log.warn("OAuth2 attributes is null for provider: {}. Creating empty map.", registrationId);
            safeAttributes = new HashMap<>();
        } else {
            safeAttributes = new HashMap<>(attributes);
        }

        log.debug("Processing OAuth2 response for provider: {}", registrationId);

        // 민감한 정보는 로그에 기록하지 않음
        if (log.isTraceEnabled()) {
            log.trace("OAuth2 attributes: {}", safeAttributes);
        }

        try {
            if ("naver".equals(registrationId)) {
                return new NaverResponse(safeAttributes);
            } else if ("kakao".equals(registrationId)) {
                return new KakaoResponse(safeAttributes);
            } else {
                log.error("Unsupported OAuth2 provider: {}", registrationId);
                return null;
            }
        } catch (Exception e) {
            log.error("Error creating OAuth2Response for provider {}: {}", registrationId, e.getMessage());

            if (log.isDebugEnabled()) {
                // 디버깅을 위한 더 상세한 정보 출력 (민감한 정보 제외)
                final Map<String, Object> finalSafeAttributes = safeAttributes;
                log.debug("Attributes structure: {}",
                        finalSafeAttributes.keySet().stream()
                                .map(key -> key + " (type: " +
                                        (finalSafeAttributes.get(key) != null ? finalSafeAttributes.get(key).getClass().getSimpleName() : "null") + ")")
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("empty"));
            }

            return null;
        }
    }
}