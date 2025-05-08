package org.lucky0111.pettalk.domain.dto.community;

import java.util.List;

public record PostPageDTO(
        List<PostResponseDTO> postList,
        int pageNo,
        int pageSize,
        int totalPages
) {
}
