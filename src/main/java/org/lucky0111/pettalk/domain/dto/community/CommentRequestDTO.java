package org.lucky0111.pettalk.domain.dto.community;

public record CommentRequestDTO(
        Long parentCommentId,
        String content
) {}
