package org.lucky0111.pettalk.domain.dto.trainer;

import org.lucky0111.pettalk.domain.entity.trainer.TrainerServiceFeeType;

import java.math.BigDecimal;

public record TrainerServiceFeeDTO(
        String serviceType,
        Integer durationMinutes,
        BigDecimal feeAmount
) {
}
