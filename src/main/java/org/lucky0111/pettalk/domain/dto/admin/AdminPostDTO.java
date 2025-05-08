package org.lucky0111.pettalk.domain.dto.admin;

import java.util.UUID;

public record AdminPostDTO(Long postId, String title, String content, UUID userId, String userName, String userNickname) {
}
