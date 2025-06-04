package org.lucky0111.pettalk.domain.dto.trainer;

import org.lucky0111.pettalk.domain.entity.trainer.Certification;

import java.time.LocalDate;
import java.util.Optional;

public record CertificationDTO(
        Long certId,
        String certName,
        String imgUrl,
        String issuingBody,
        LocalDate issueDate
) {
    // Certification 엔티티 객체를 받아서 CertificationDto Record 객체를 생성하는 정적 팩토리 메소드
    public static Optional<CertificationDTO> fromEntity(Certification certification) {
        if (certification == null) {
            return Optional.empty();
        }

        // Certification 엔티티 필드 값들을 가져와서 CertificationDto Record의 생성자로 전달
        return Optional.of(new CertificationDTO(
                certification.getCertId(),
                certification.getCertName(),
                certification.getFileUrl(),
                certification.getIssuingBody(),
                certification.getIssueDate()
        ));
    }
}