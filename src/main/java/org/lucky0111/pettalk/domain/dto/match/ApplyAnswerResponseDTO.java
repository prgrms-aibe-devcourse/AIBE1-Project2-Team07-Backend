package org.lucky0111.pettalk.domain.dto.match;

public record ApplyAnswerResponseDTO(
        Long responseId,
        Long applyId,
        String userNickname,
        String trainerName,
        String trainerNickname,
        String trainerImageUrl,
        String content,
        String createdAt,
        String updatedAt
) {}
