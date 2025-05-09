package org.lucky0111.pettalk.domain.dto.review;

import org.lucky0111.pettalk.exception.CustomException;
import org.springframework.http.HttpStatus;

public record ReviewRequestDTO(
        Long applyId,
        Integer rating,
        String comment,
        String reviewImageUrl
) {
        public ReviewRequestDTO {
                if (rating == null || rating < 1 || rating > 5) {
                        throw new CustomException("평점은 1~5 사이여야 합니다.", HttpStatus.BAD_REQUEST);
                }
        }
}
