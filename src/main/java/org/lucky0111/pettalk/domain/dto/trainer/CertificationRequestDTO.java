package org.lucky0111.pettalk.domain.dto.trainer;

import java.time.LocalDate;

public record CertificationRequestDTO(
        String certName, // 자격증 이름
        String issuingBody, // 발급 기관
        LocalDate issueDate // 취득 일자
        // 자격증 첨부파일은 별도로 처리
) {
}
