package org.lucky0111.pettalk.domain.dto.trainer;

import java.math.BigDecimal;

public record TrainerServiceFeeDTO(
        String serviceType,
        Integer durationMinutes,
        BigDecimal feeAmount
) {
}
