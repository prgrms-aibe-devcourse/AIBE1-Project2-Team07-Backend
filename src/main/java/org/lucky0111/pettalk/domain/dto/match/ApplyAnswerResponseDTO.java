package org.lucky0111.pettalk.domain.dto.match;

public record ApplyAnswerResponseDTO(
        Long responseId,
        Long applyId,
        String trainerName,
        String trainerImageUrl,
        String content,
        String createdAt,
        String updatedAt
) {}
