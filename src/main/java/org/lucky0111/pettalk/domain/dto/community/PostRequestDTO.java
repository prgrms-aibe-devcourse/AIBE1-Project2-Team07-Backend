package org.lucky0111.pettalk.domain.dto.community;

import java.util.List;

public record PostRequestDTO(
        Long postCategoryId,
        Long petCategoryId,
        String title,
        String content,
        String imageUrl,
        String videoUrl,
        List<Long> tagIds
) {}