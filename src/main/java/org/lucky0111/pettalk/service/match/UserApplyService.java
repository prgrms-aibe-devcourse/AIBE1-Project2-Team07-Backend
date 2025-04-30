package org.lucky0111.pettalk.service.match;

import org.lucky0111.pettalk.domain.common.Status;
import org.lucky0111.pettalk.domain.dto.match.UserApplyRequestDTO;
import org.lucky0111.pettalk.domain.dto.match.UserApplyResponseDTO;
import org.lucky0111.pettalk.domain.entity.match.UserApply;

import java.nio.file.AccessDeniedException;
import java.util.List;


public interface UserApplyService {
    UserApplyResponseDTO createApply(UserApplyRequestDTO requestDTO);

    List<UserApplyResponseDTO> getUserApplies();

    List<UserApplyResponseDTO> getTrainerApplies();

    UserApplyResponseDTO updateApplyStatus(Long applyId, Status status);

    UserApplyResponseDTO convertToResponseDTO(UserApply userApply);

    UserApplyResponseDTO deleteApply(Long applyId) throws AccessDeniedException;
}
