package org.lucky0111.pettalk.domain.dto.review;

import java.util.UUID;

public record ReviewResponseDTO(
        Long reviewId,
        Long applyId,
        UUID userId,
        String userName,
        UUID trainerId,
        String trainerName,
        Integer rating,
        String title,
        String comment,
        String reviewImageUrl,
        Integer likeCount,
        Boolean hasLiked,
        String createdAt,
        String updatedAt
) {
}
