package org.lucky0111.pettalk.domain.dto.trainer;

import org.lucky0111.pettalk.domain.entity.trainer.Certification;

import java.time.LocalDate;

// Record는 불변 객체이며, 필드(컴포넌트)들을 정의하고 자동 생성되는 멤버들을 활용합니다.
public record CertificationDTO(
        Long certId, // Certification 엔티티의 cert_id (bigint) -> Long
        String certName, // Certification 엔티티의 cert_name (varchar) -> String
        String issuingBody, // Certification 엔티티의 issuing_body (varchar) -> String
        LocalDate issueDate // Certification 엔티티의 issue_date (date) -> LocalDate 매핑
) {
    // Record는 컴포넌트(필드)들을 인자로 받는 생성자가 자동으로 생성됩니다.
    // 예: new CertificationDto(certId, certName, issuingBody, issueDate)

    // Certification 엔티티 객체를 받아서 CertificationDto Record 객체를 생성하는 정적 팩토리 메소드
    public static CertificationDTO fromEntity(Certification certification) {
        if (certification == null) {
            return null; // 엔티티가 null이면 DTO도 null 반환 또는 적절히 처리
        }

        // Certification 엔티티 필드 값들을 가져와서 CertificationDto Record의 생성자로 전달
        return new CertificationDTO(
                certification.getCertId(), // 엔티티에서 ID 값 가져오기
                certification.getCertName(), // 엔티티에서 자격증 이름 가져오기
                certification.getIssuingBody(), // 엔티티에서 발급 기관 가져오기
                // 엔티티의 날짜 타입(java.sql.Date 또는 java.time.LocalDate)을 Record의 LocalDate 타입으로 변환
                // Certification 엔티티의 issueDate 필드가 java.sql.Date 타입이라면:
                // certification.getIssueDate() != null ? certification.getIssueDate().toLocalDate() : null,
                // Certification 엔티티의 issueDate 필드가 이미 java.time.LocalDate 타입이라면:
                certification.getIssueDate() // 엔티티에서 취득일자 가져오기 (LocalDate라고 가정)
                // 만약 엔티티 필드가 Date 타입인데 getIssueDate()가 없다면, 필드에 직접 접근하거나 (가능하다면) Getter를 추가해야 합니다.
        );
    }

    // Record는 기본적으로 필드명() 형태의 접근자 메소드를 자동 생성합니다.
    // 예: certDto.certId(), certDto.certName() 등
}