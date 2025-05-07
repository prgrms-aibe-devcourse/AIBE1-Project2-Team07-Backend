package org.lucky0111.pettalk.domain.dto.trainer;

import org.lucky0111.pettalk.domain.entity.trainer.Certification;

import java.time.LocalDate;

// Record는 불변 객체이며, 필드(컴포넌트)들을 정의하고 자동 생성되는 멤버들을 활용합니다.
public record CertificationDTO(
        Long certId,
        String certName,
        String issuingBody,
        LocalDate issueDate
) {
    // Certification 엔티티 객체를 받아서 CertificationDto Record 객체를 생성하는 정적 팩토리 메소드
    public static CertificationDTO fromEntity(Certification certification) {
        if (certification == null) {
            return null;
        }

        // Certification 엔티티 필드 값들을 가져와서 CertificationDto Record의 생성자로 전달
        return new CertificationDTO(
                certification.getCertId(),
                certification.getCertName(),
                certification.getIssuingBody(),
                certification.getIssueDate()
        );
    }
}