package org.lucky0111.pettalk.domain.dto.admin;

import java.time.LocalDate;
import java.util.UUID;

public record AdminCertificationDTO(Long certId, String certName, String issuingBody, LocalDate issueDate, String fileUrl, Boolean approved, UUID trainerId, String trainerName, String trainerNickname) {
}
