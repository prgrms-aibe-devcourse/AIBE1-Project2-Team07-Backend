package org.lucky0111.pettalk.domain.dto.community;

public record CommentRequestDTO(
        Long postId,
        Long parentCommentId,
        String content
) {}
