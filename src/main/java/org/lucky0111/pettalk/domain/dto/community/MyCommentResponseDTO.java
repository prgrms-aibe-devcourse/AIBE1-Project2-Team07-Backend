package org.lucky0111.pettalk.domain.dto.community;

public record MyCommentResponseDTO(
        Long postId,
        Long commentId,
        String postTitle,
        String postContent,
        String commentContent,
        String createdAt,
        String updatedAt
) {
}
