package org.lucky0111.pettalk.domain.dto.match;

import org.lucky0111.pettalk.domain.common.ServiceType;

public record UserApplyRequestDTO(
        String trainerNickName,
        ServiceType serviceType,
        String petType,
        String petBreed,
        Integer petMonthAge,
        String content
) {
}
