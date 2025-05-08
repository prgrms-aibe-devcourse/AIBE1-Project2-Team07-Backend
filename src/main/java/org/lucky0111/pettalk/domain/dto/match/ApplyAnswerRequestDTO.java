package org.lucky0111.pettalk.domain.dto.match;

import org.lucky0111.pettalk.domain.common.ApplyStatus;
import org.lucky0111.pettalk.domain.common.ApplyReason;

public record ApplyAnswerRequestDTO(
        Long applyId,
        ApplyStatus applyStatus,
        String content,
        ApplyReason applyReason
) {}
