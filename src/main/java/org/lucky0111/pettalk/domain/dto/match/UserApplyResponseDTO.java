package org.lucky0111.pettalk.domain.dto.match;

import org.lucky0111.pettalk.domain.common.Status;

import java.util.UUID;

public record UserApplyResponseDTO(
        Long applyId,
        UUID userId,
        String userName,
        UUID trainerId,
        String trainerName,
        String content,
        String imageUrl,
        Status status,
        String createdAt,
        String updatedAt
){}
