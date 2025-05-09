package org.lucky0111.pettalk.domain.dto.trainer;

import jakarta.validation.constraints.NotNull;
import org.lucky0111.pettalk.domain.common.ServiceType;

public record ServiceFeeUpdateDTO(
        @NotNull
        ServiceType serviceType,
        @NotNull
        int time,
        @NotNull
        int price
) {
}
