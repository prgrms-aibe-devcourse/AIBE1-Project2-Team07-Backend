package org.lucky0111.pettalk.service.match;

import org.lucky0111.pettalk.domain.common.ApplyStatus;
import org.lucky0111.pettalk.domain.dto.match.UserApplyRequestDTO;
import org.lucky0111.pettalk.domain.dto.match.UserApplyResponseDTO;
import org.lucky0111.pettalk.domain.entity.match.UserApply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserApplyService {
    UserApplyResponseDTO createApply(UserApplyRequestDTO requestDTO);

    List<UserApplyResponseDTO> getUserApplies();

    List<UserApplyResponseDTO> getUserAppliesByStatus(ApplyStatus applyStatus);

    Page<UserApplyResponseDTO> getUserAppliesPaged(Pageable pageable);

    Page<UserApplyResponseDTO> getUserAppliesByStatusPaged(ApplyStatus applyStatus, Pageable pageable);

    List<UserApplyResponseDTO> getTrainerApplies();

    List<UserApplyResponseDTO> getTrainerAppliesByStatus(ApplyStatus applyStatus);

    Page<UserApplyResponseDTO> getTrainerAppliesPaged(Pageable pageable);

    Page<UserApplyResponseDTO> getTrainerAppliesByStatusPaged(ApplyStatus applyStatus, Pageable pageable);

    UserApplyResponseDTO updateApplyStatus(Long applyId, ApplyStatus applyStatus);

    UserApplyResponseDTO deleteApply(Long applyId);
}