package org.lucky0111.pettalk.domain.dto.match;

import org.lucky0111.pettalk.domain.common.ApplyStatus;

public record UserApplyResponseDTO(
        Long applyId,
        String userNickname,
        String userImageUrl,
        String trainerNickname,
        String trainerImageUrl,
        String serviceType,
        String petType,
        String petBreed,
        Integer petMonthAge,
        String content,
        String imageUrl,
        ApplyStatus applyStatus,
        boolean hasReviewed,
        String createdAt,
        String updatedAt
){}
