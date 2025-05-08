package org.lucky0111.pettalk.domain.dto.admin;

import java.util.UUID;

public record AdminCommentDTO(Long commentId, String content, UUID userId, String userName, String userNickname) {
}
