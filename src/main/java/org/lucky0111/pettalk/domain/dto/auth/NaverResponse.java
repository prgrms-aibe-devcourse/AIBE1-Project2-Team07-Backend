package org.lucky0111.pettalk.domain.dto.auth;

import java.util.HashMap;
import java.util.Map;

public class NaverResponse implements OAuth2Response {

    private final Map<String, Object> attribute;
    private final Map<String, Object> originalAttributes;

    public NaverResponse(Map<String, Object> attributes) {
        // attributes가 null이면 빈 맵으로 초기화
        this.originalAttributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();

        // 응답 형식 디버깅
        System.out.println("Naver original attributes: " + this.originalAttributes);

        // 안전하게 response 데이터 추출
        if (this.originalAttributes.containsKey("response") &&
                this.originalAttributes.get("response") instanceof Map) {

            // response 맵을 복사하여 새 맵 생성 (불변 맵 문제 방지)
            this.attribute = new HashMap<>((Map<String, Object>) this.originalAttributes.get("response"));
            System.out.println("Naver response data: " + this.attribute);
        } else {
            // Naver의 응답 형식이 예상과 다른 경우 처리
            System.err.println("Unexpected Naver response format. Using original attributes.");
            this.attribute = this.originalAttributes;
        }
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        if (attribute != null && attribute.containsKey("id")) {
            return attribute.get("id").toString();
        } else if (originalAttributes.containsKey("id")) {
            return originalAttributes.get("id").toString();
        }

        // 응답에서 id를 찾지 못한 경우 오류 메시지 출력
        System.err.println("No provider ID found in Naver response: " + originalAttributes);
        return "unknown"; // 혹은 null을 반환하고 호출자가 처리하도록 함
    }

    @Override
    public String getEmail() {
        if (attribute != null && attribute.containsKey("email")) {
            return attribute.get("email").toString();
        } else if (originalAttributes.containsKey("email")) {
            return originalAttributes.get("email").toString();
        }

        return null;
    }

    @Override
    public String getName() {
        if (attribute != null && attribute.containsKey("name")) {
            return attribute.get("name").toString();
        } else if (originalAttributes.containsKey("name")) {
            return originalAttributes.get("name").toString();
        }

        String providerId = getProviderId();
        if ("unknown".equals(providerId)) {
            return "NaverUser";
        }

        return "NaverUser_" + providerId.substring(0, Math.min(providerId.length(), 6));
    }
}