package org.lucky0111.pettalk.service.match;

import jakarta.servlet.http.HttpServletRequest;
import org.lucky0111.pettalk.domain.common.Status;
import org.lucky0111.pettalk.domain.dto.match.UserApplyRequestDTO;
import org.lucky0111.pettalk.domain.dto.match.UserApplyResponseDTO;
import org.lucky0111.pettalk.domain.entity.match.UserApply;

import java.nio.file.AccessDeniedException;
import java.util.List;


public interface UserApplyService {
    UserApplyResponseDTO createApply(UserApplyRequestDTO requestDTO, HttpServletRequest request);

    List<UserApplyResponseDTO> getUserApplies(HttpServletRequest request);

    List<UserApplyResponseDTO> getTrainerApplies(HttpServletRequest request);

    UserApplyResponseDTO updateApplyStatus(Long applyId, Status status,HttpServletRequest request);

    UserApplyResponseDTO convertToResponseDTO(UserApply userApply);

    UserApplyResponseDTO deleteApply(Long applyId,HttpServletRequest request);
}
