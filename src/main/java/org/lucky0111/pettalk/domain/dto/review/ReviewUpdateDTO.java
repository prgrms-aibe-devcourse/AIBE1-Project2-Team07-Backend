package org.lucky0111.pettalk.domain.dto.review;

public record ReviewUpdateDTO(
        Integer rating,
        String title,
        String comment,
        String reviewImageUrl
) {
}
