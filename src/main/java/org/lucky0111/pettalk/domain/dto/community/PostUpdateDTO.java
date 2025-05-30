package org.lucky0111.pettalk.domain.dto.community;

import org.lucky0111.pettalk.domain.common.PetCategory;
import org.lucky0111.pettalk.domain.common.PostCategory;

import java.util.List;

public record PostUpdateDTO(
        PostCategory postCategory,
        PetCategory petCategory,
        String title,
        String content,
        String deleteImageUrls,
        boolean deleteVideo,
        List<Long> tagIds
) {}