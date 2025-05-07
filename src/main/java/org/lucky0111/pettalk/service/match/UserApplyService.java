package org.lucky0111.pettalk.service.match;

import org.lucky0111.pettalk.domain.common.Status;
import org.lucky0111.pettalk.domain.dto.match.UserApplyRequestDTO;
import org.lucky0111.pettalk.domain.dto.match.UserApplyResponseDTO;
import org.lucky0111.pettalk.domain.entity.match.UserApply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserApplyService {
    UserApplyResponseDTO createApply(UserApplyRequestDTO requestDTO);

    List<UserApplyResponseDTO> getUserApplies();

    List<UserApplyResponseDTO> getUserAppliesByStatus(Status status);

    Page<UserApplyResponseDTO> getUserAppliesPaged(Pageable pageable);

    Page<UserApplyResponseDTO> getUserAppliesByStatusPaged(Status status, Pageable pageable);

    List<UserApplyResponseDTO> getTrainerApplies();

    List<UserApplyResponseDTO> getTrainerAppliesByStatus(Status status);

    Page<UserApplyResponseDTO> getTrainerAppliesPaged(Pageable pageable);

    Page<UserApplyResponseDTO> getTrainerAppliesByStatusPaged(Status status, Pageable pageable);

    UserApplyResponseDTO updateApplyStatus(Long applyId, Status status);

    UserApplyResponseDTO deleteApply(Long applyId);

    UserApplyResponseDTO convertToResponseDTO(UserApply userApply);
}