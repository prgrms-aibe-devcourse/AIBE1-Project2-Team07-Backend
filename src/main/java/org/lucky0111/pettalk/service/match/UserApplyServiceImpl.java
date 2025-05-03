package org.lucky0111.pettalk.service.match;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.common.ErrorCode;
import org.lucky0111.pettalk.domain.common.Status;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.dto.match.UserApplyRequestDTO;
import org.lucky0111.pettalk.domain.dto.match.UserApplyResponseDTO;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.domain.entity.trainer.Trainer;
import org.lucky0111.pettalk.domain.entity.match.UserApply;
import org.lucky0111.pettalk.exception.CustomException;
import org.lucky0111.pettalk.repository.match.UserApplyRepository;
import org.lucky0111.pettalk.repository.trainer.TrainerRepository;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.lucky0111.pettalk.util.auth.JWTUtil;
import org.lucky0111.pettalk.util.error.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserApplyServiceImpl implements UserApplyService {

    private final UserApplyRepository userApplyRepository;
    private final PetUserRepository petUserRepository;
    private final TrainerRepository trainerRepository;
    private final JWTUtil jwtUtil;

    @Transactional
    public UserApplyResponseDTO createApply(UserApplyRequestDTO requestDTO) {
        PetUser currentUser = getCurrentUser();

        Trainer trainer = trainerRepository.findById(requestDTO.trainerId())
                .orElseThrow(() -> ExceptionUtils.of(ErrorCode.TRAINER_NOT_FOUND));

        if (userApplyRepository.existsByPetUser_userIdAndTrainer_trainerIdAndStatus(
                currentUser.getUserId(),
                requestDTO.trainerId(),
                Status.PENDING
        )) {
            throw ExceptionUtils.of(ErrorCode.APPLY_ALREADY_EXISTS);
        };

        UserApply userApply = new UserApply();
        userApply.setPetUser(currentUser);
        userApply.setTrainer(trainer);
        userApply.setContent(requestDTO.content());
        userApply.setImageUrl(requestDTO.imageUrl());
        userApply.setStatus(Status.PENDING);

        UserApply savedApply = userApplyRepository.save(userApply);

        return convertToResponseDTO(savedApply);
    }

    @Transactional(readOnly = true)
    public List<UserApplyResponseDTO> getUserApplies() {
        PetUser currentUser = getCurrentUser();

        List<UserApply> userApplies = userApplyRepository.findByPetUser_UserId(currentUser.getUserId());

        return userApplies.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserApplyResponseDTO> getTrainerApplies() {
        PetUser currentUser = getCurrentUser();

        List<UserApply> trainerApplies = userApplyRepository.findByTrainer_TrainerId(currentUser.getUserId());

        return trainerApplies.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserApplyResponseDTO updateApplyStatus(Long applyId, Status status) {
        PetUser currentUser = getCurrentUser();

        // 신청 정보 조회
        UserApply userApply = userApplyRepository.findById(applyId)
                .orElseThrow(() -> ExceptionUtils.of(ErrorCode.APPLY_NOT_FOUND));

        // 트레이너 권한 확인
        if (!userApply.getTrainer().getTrainerId().equals(currentUser.getUserId())) {
            throw ExceptionUtils.of(ErrorCode.PERMISSION_DENIED);
        }

        // 상태 업데이트
        userApply.setStatus(status);

        // 저장
        UserApply updatedApply = userApplyRepository.save(userApply);

        // 응답 DTO 변환 및 반환
        return convertToResponseDTO(updatedApply);
    }

    public UserApplyResponseDTO deleteApply(Long applyId) {
        PetUser currentUser = getCurrentUser();

        UserApply userApply = userApplyRepository.findById(applyId)
                .orElseThrow(() -> ExceptionUtils.of(ErrorCode.APPLY_NOT_FOUND));

        // 현재 사용자가 신청서의 작성자인지 확인
        if (!userApply.getPetUser().getUserId().equals(currentUser.getUserId())) {
            throw ExceptionUtils.of(ErrorCode.PERMISSION_DENIED);
        }

        // 삭제 전에 응답용 DTO 생성
        UserApplyResponseDTO responseDTO = convertToResponseDTO(userApply);

        // 신청서 삭제
        userApplyRepository.delete(userApply);

        return responseDTO;
    }

    private UUID getCurrentUserUUID() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal() instanceof CustomOAuth2User userDetails) {
            return userDetails.getUserId();
        }

        throw ExceptionUtils.of(ErrorCode.UNAUTHORIZED);
    }

    private PetUser getCurrentUser() {
        UUID currentUserUUID = getCurrentUserUUID();
        return petUserRepository.findById(currentUserUUID)
                .orElseThrow(() -> ExceptionUtils.of(ErrorCode.USER_NOT_FOUND));
    }

    // UserApply 엔티티를 ResponseDTO로 변환하는 메서드
    public UserApplyResponseDTO convertToResponseDTO(UserApply userApply) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // null 체크 후 날짜 포맷팅
        String createdAtStr = userApply.getCreatedAt() != null ?
                userApply.getCreatedAt().format(formatter) : null;

        String updatedAtStr = userApply.getUpdatedAt() != null ?
                userApply.getUpdatedAt().format(formatter) : null;

        return new UserApplyResponseDTO(
                userApply.getApplyId(),
                userApply.getPetUser().getUserId(),
                userApply.getPetUser().getName(),
                userApply.getTrainer().getTrainerId(),
                userApply.getTrainer().getUser().getName(),
                userApply.getPetType(),
                userApply.getPetBreed(),
                userApply.getPetMonthAge(),
                userApply.getContent(),
                userApply.getImageUrl(),
                userApply.getStatus(),
                createdAtStr,
                updatedAtStr
        );
    }



}
