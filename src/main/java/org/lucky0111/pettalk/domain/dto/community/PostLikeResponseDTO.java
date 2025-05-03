package org.lucky0111.pettalk.domain.dto.community;

import java.util.UUID;

public record PostLikeResponseDTO(
        Long likeId,
        Long postId,
        UUID userId,
        String createdAt,
        Boolean liked
) {}
