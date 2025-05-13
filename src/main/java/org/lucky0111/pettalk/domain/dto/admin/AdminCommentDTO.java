package org.lucky0111.pettalk.domain.dto.admin;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminCommentDTO(Long commentId, LocalDateTime createdAt, String content, UUID userId, String userName, String userNickname) {
}
