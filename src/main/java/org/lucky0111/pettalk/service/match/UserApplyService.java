package org.lucky0111.pettalk.service.match;

import org.lucky0111.pettalk.domain.common.Status;
import org.lucky0111.pettalk.domain.dto.match.UserApplyRequestDTO;
import org.lucky0111.pettalk.domain.dto.match.UserApplyResponseDTO;
import org.lucky0111.pettalk.domain.entity.match.UserApply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserApplyService {
    /**
     * 새로운 매칭 신청서 생성
     * @param requestDTO 신청서 정보
     * @return 생성된 신청서 응답 DTO
     */
    UserApplyResponseDTO createApply(UserApplyRequestDTO requestDTO);

    /**
     * 현재 로그인한 사용자의 모든 신청 목록 조회
     * @return 신청서 응답 DTO 목록
     */
    List<UserApplyResponseDTO> getUserApplies();

    /**
     * 현재 로그인한 사용자의 상태별 신청 목록 조회
     * @param status 신청 상태
     * @return 신청서 응답 DTO 목록
     */
    List<UserApplyResponseDTO> getUserAppliesByStatus(Status status);

    /**
     * 현재 로그인한 사용자의 신청 목록을 페이징하여 조회
     * @param pageable 페이징 정보
     * @return 페이징된 신청서 응답 DTO
     */
    Page<UserApplyResponseDTO> getUserAppliesPaged(Pageable pageable);

    /**
     * 현재 로그인한 사용자의 상태별 신청 목록을 페이징하여 조회
     * @param status 신청 상태
     * @param pageable 페이징 정보
     * @return 페이징된 신청서 응답 DTO
     */
    Page<UserApplyResponseDTO> getUserAppliesByStatusPaged(Status status, Pageable pageable);

    /**
     * 현재 로그인한 트레이너에게 온 모든 신청 목록 조회
     * @return 신청서 응답 DTO 목록
     */
    List<UserApplyResponseDTO> getTrainerApplies();

    /**
     * 현재 로그인한 트레이너에게 온 상태별 신청 목록 조회
     * @param status 신청 상태
     * @return 신청서 응답 DTO 목록
     */
    List<UserApplyResponseDTO> getTrainerAppliesByStatus(Status status);

    /**
     * 현재 로그인한 트레이너에게 온 신청 목록을 페이징하여 조회
     * @param pageable 페이징 정보
     * @return 페이징된 신청서 응답 DTO
     */
    Page<UserApplyResponseDTO> getTrainerAppliesPaged(Pageable pageable);

    /**
     * 현재 로그인한 트레이너에게 온 상태별 신청 목록을 페이징하여 조회
     * @param status 신청 상태
     * @param pageable 페이징 정보
     * @return 페이징된 신청서 응답 DTO
     */
    Page<UserApplyResponseDTO> getTrainerAppliesByStatusPaged(Status status, Pageable pageable);

    /**
     * 매칭 신청 상태 업데이트
     * @param applyId 신청 ID
     * @param status 변경할 상태
     * @return 업데이트된 신청서 응답 DTO
     */
    UserApplyResponseDTO updateApplyStatus(Long applyId, Status status);

    /**
     * 매칭 신청 삭제
     * @param applyId 신청 ID
     * @return 삭제된 신청서 응답 DTO
     */
    UserApplyResponseDTO deleteApply(Long applyId);

    /**
     * UserApply 엔티티를 ResponseDTO로 변환
     * @param userApply 변환할 엔티티
     * @return 변환된 DTO
     */
    UserApplyResponseDTO convertToResponseDTO(UserApply userApply);
}