package org.lucky0111.pettalk.domain.dto.community;

import java.util.List;
import java.util.UUID;

public record PostResponseDTO(
        Long postId,
        UUID userId,
        String userName,
        String userNickname,
        String profileImageUrl,
        String postCategoryName,
        String petCategoryName,
        String title,
        String content,
        String imageUrl,
        String videoUrl,
        Integer likeCount,
        Integer commentCount,
        Boolean hasLiked,
        List<String> tags,
        String createdAt,
        String updatedAt
) {}
