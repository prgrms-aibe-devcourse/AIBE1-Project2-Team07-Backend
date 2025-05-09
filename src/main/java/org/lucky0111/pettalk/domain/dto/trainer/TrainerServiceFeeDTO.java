package org.lucky0111.pettalk.domain.dto.trainer;

public record TrainerServiceFeeDTO(
        String serviceType,
        Integer durationMinutes,
        Integer feeAmount
) {
}
