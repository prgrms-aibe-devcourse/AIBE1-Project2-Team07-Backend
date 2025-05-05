package org.lucky0111.pettalk.domain.dto.community;

import java.util.List;

public record CommentsResponseDTO(
        List<CommentResponseDTO> comments,
        Long nextCursor,
        boolean hasMore
) {}
