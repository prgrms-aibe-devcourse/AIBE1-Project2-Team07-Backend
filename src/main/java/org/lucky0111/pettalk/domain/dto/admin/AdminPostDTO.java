package org.lucky0111.pettalk.domain.dto.admin;

import org.lucky0111.pettalk.domain.common.PetCategory;
import org.lucky0111.pettalk.domain.common.PostCategory;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminPostDTO(Long postId, PostCategory postCategory, PetCategory petCategory, LocalDateTime createdAt, String title, String content, Integer likeCount, Integer commentCount, UUID userId, String userName, String userNickname) {
}
