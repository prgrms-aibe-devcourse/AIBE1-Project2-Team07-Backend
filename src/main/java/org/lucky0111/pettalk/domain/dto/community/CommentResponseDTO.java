package org.lucky0111.pettalk.domain.dto.community;

import java.util.List;
import java.util.UUID;

public record CommentResponseDTO(
        Long commentId,
        Long postId,
        String userName,
        String userNickname,
        String profileImageUrl,
        Long parentCommentId,
        String content,
        List<CommentResponseDTO> replies,
        int replyCount,
        String createdAt,
        String updatedAt
) {}
