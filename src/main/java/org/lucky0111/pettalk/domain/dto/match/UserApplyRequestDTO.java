package org.lucky0111.pettalk.domain.dto.match;

import java.util.UUID;

public record UserApplyRequestDTO(
        String trainerName,
        String petType,
        String petBreed,
        Integer petMonthAge,
        String content,
        String imageUrl
) {
}
