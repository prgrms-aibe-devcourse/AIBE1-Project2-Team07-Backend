package org.lucky0111.pettalk.domain.dto.trainer;

import java.util.List;

public record TrainerApplicationRequestDTO(
        String introduction,
        int experienceYears,
        // List<String> specializations, // 전문 분야 목록 (저장 방식에 따라 필요)
        // int consultationCostCredits, // 상담 비용 (선택 사항)
        // String availableTime, // 가능 시간 (선택 사항)
        List<CertificationRequestDTO> certifications // 제출할 자격증 목록

) {
}
