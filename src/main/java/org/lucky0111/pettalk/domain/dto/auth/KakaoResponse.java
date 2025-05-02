package org.lucky0111.pettalk.domain.dto.auth;

import java.util.HashMap;
import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attributes;
    private final Map<String, Object> properties;
    private final Map<String, Object> kakao_account;

    public KakaoResponse(Map<String, Object> attributes) {
        // attributes가 null이면 빈 맵으로 초기화
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();

        // 응답 형식 디버깅
        System.out.println("Kakao original attributes: " + this.attributes);

        // 안전하게 properties와 kakao_account 데이터 추출
        if (this.attributes.containsKey("properties") &&
                this.attributes.get("properties") instanceof Map) {

            // properties 맵을 복사하여 새 맵 생성 (불변 맵 문제 방지)
            this.properties = new HashMap<>((Map<String, Object>) this.attributes.get("properties"));
            System.out.println("Kakao properties data: " + this.properties);
        } else {
            this.properties = new HashMap<>();
            System.err.println("No properties data found in Kakao response");
        }

        if (this.attributes.containsKey("kakao_account") &&
                this.attributes.get("kakao_account") instanceof Map) {

            // kakao_account 맵을 복사하여 새 맵 생성 (불변 맵 문제 방지)
            this.kakao_account = new HashMap<>((Map<String, Object>) this.attributes.get("kakao_account"));
            System.out.println("Kakao account data: " + this.kakao_account);
        } else {
            this.kakao_account = new HashMap<>();
            System.err.println("No kakao_account data found in Kakao response");
        }
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        if (attributes.containsKey("id")) {
            return attributes.get("id").toString();
        }

        // 응답에서 id를 찾지 못한 경우 오류 메시지 출력
        System.err.println("No provider ID found in Kakao response: " + attributes);
        return "unknown";
    }

    @Override
    public String getEmail() {
        if (kakao_account != null && kakao_account.containsKey("email")) {
            return kakao_account.get("email").toString();
        }

        System.out.println("No email found in Kakao response");
        return null;
    }

    @Override
    public String getName() {
        // properties에서 nickname 정보를 가져옵니다
        if (properties != null && properties.containsKey("nickname")) {
            return properties.get("nickname").toString();
        }

        // kakao_account에서 profile의 nickname을 시도
        if (kakao_account != null && kakao_account.containsKey("profile") &&
                kakao_account.get("profile") instanceof Map) {

            Map<String, Object> profile = (Map<String, Object>) kakao_account.get("profile");
            if (profile.containsKey("nickname")) {
                return profile.get("nickname").toString();
            }
        }

        String providerId = getProviderId();
        if ("unknown".equals(providerId)) {
            return "KakaoUser";
        }

        return "KakaoUser_" + providerId.substring(0, Math.min(providerId.length(), 6));
    }
}