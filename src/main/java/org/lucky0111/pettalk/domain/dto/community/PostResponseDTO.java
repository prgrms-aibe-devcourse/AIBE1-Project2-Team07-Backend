package org.lucky0111.pettalk.domain.dto.community;

import org.lucky0111.pettalk.domain.common.PetCategory;
import org.lucky0111.pettalk.domain.common.PostCategory;

import java.util.List;
import java.util.UUID;

public record PostResponseDTO(
        Long postId,
        String userName,
        String userNickname,
        String profileImageUrl,
        PostCategory postCategory,
        PetCategory petCategory,
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
