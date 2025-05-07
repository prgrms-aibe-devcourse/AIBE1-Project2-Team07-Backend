package org.lucky0111.pettalk.domain.dto.match;

import org.lucky0111.pettalk.domain.common.ApplyStatus;

import java.util.UUID;

public record UserApplyResponseDTO(
        Long applyId,
        String userName,
        String trainerName,
        String petType,
        String petBreed,
        Integer petMonthAge,
        String content,
        String imageUrl,
        ApplyStatus applyStatus,
        String createdAt,
        String updatedAt
){}
