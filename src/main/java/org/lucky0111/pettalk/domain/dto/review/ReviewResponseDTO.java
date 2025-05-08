package org.lucky0111.pettalk.domain.dto.review;

public record ReviewResponseDTO(
        Long reviewId,
        Long applyId,
        String userNickname,
        String userImageUrl,
        String trainerName,
        String trainerNickname,
        String trainerImageUrl,
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
