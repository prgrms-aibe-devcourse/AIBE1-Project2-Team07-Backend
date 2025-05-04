package org.lucky0111.pettalk.service.match;

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
import org.lucky0111.pettalk.repository.match.UserApplyRepository;
import org.lucky0111.pettalk.repository.trainer.TrainerRepository;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.lucky0111.pettalk.util.auth.JWTUtil;
import org.lucky0111.pettalk.util.error.ExceptionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private static final String LOG_PREFIX = "[UserApplyService]";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
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
        userApply.setPetType(requestDTO.petType());
        userApply.setPetBreed(requestDTO.petBreed());
        userApply.setPetMonthAge(requestDTO.petMonthAge());
        userApply.setContent(requestDTO.content());
        userApply.setImageUrl(requestDTO.imageUrl());
        userApply.setStatus(Status.PENDING);

        UserApply savedApply = userApplyRepository.save(userApply);

        return convertToResponseDTO(savedApply);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserApplyResponseDTO> getUserApplies() {
        PetUser currentUser = getCurrentUser();

        List<UserApply> userApplies = userApplyRepository.findByPetUser_UserIdWithRelations(currentUser.getUserId());
        log.info("{} 조회된 신청 수: {}", LOG_PREFIX, userApplies.size());

        // DTO 변환 및 반환
        return userApplies.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserApplyResponseDTO> getTrainerApplies() {
        PetUser currentUser = getCurrentUser();

        List<UserApply> trainerApplies = userApplyRepository.findByTrainer_TrainerId(currentUser.getUserId());

        return trainerApplies.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserApplyResponseDTO updateApplyStatus(Long applyId, Status status) {
        log.info("{} 매칭 신청 상태 업데이트: applyId={}, status={}", LOG_PREFIX, applyId, status);

        PetUser currentUser = getCurrentUser();

        UserApply userApply = userApplyRepository.findByIdWithRelations(applyId)
                .orElseThrow(() -> ExceptionUtils.of(ErrorCode.APPLY_NOT_FOUND));

        if (!userApply.getTrainer().getTrainerId().equals(currentUser.getUserId())) {
            log.warn("{} 권한 없음: user={}, trainerId={}",
                    LOG_PREFIX, currentUser.getUserId(), userApply.getTrainer().getTrainerId());
            throw ExceptionUtils.of(ErrorCode.PERMISSION_DENIED);
        }

        userApply.setStatus(status);
        UserApply updatedApply = userApplyRepository.save(userApply);
        log.info("{} 상태 업데이트 완료: applyId={}, status={}", LOG_PREFIX, applyId, status);

        return convertToResponseDTO(updatedApply);
    }

    @Override
    @Transactional
    public UserApplyResponseDTO deleteApply(Long applyId) {
        log.info("{} 매칭 신청 삭제 요청: applyId={}", LOG_PREFIX, applyId);

        PetUser currentUser = getCurrentUser();

        UserApply userApply = userApplyRepository.findByIdWithRelations(applyId)
                .orElseThrow(() -> ExceptionUtils.of(ErrorCode.APPLY_NOT_FOUND));

        if (!userApply.getPetUser().getUserId().equals(currentUser.getUserId())) {
            log.warn("{} 권한 없음: user={}, applyUserId={}",
                    LOG_PREFIX, currentUser.getUserId(), userApply.getPetUser().getUserId());
            throw ExceptionUtils.of(ErrorCode.PERMISSION_DENIED);
        }

        UserApplyResponseDTO responseDTO = convertToResponseDTO(userApply);

        userApplyRepository.delete(userApply);
        log.info("{} 매칭 신청 삭제 완료: applyId={}", LOG_PREFIX, applyId);

        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserApplyResponseDTO> getUserAppliesByStatus(Status status) {
        log.info("{} 사용자 상태별 매칭 신청 목록 조회: status={}", LOG_PREFIX, status);

        // 현재 사용자 정보 가져오기
        PetUser currentUser = getCurrentUser();

        // 최적화된 쿼리로 사용자의 상태별 신청 목록 조회
        List<UserApply> userApplies = userApplyRepository.findByPetUser_UserIdAndStatusWithRelations(
                currentUser.getUserId(), status);
        log.info("{} 조회된 신청 수: {}", LOG_PREFIX, userApplies.size());

        // DTO 변환 및 반환
        return userApplies.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserApplyResponseDTO> getUserAppliesPaged(Pageable pageable) {
        log.info("{} 사용자 매칭 신청 목록 페이징 조회: page={}, size={}",
                LOG_PREFIX, pageable.getPageNumber(), pageable.getPageSize());

        // 현재 사용자 정보 가져오기
        PetUser currentUser = getCurrentUser();

        // 페이징 처리된 쿼리로 사용자의 신청 목록 조회
        Page<UserApply> userAppliesPage = userApplyRepository.findByPetUser_UserIdWithRelationsPaged(
                currentUser.getUserId(), pageable);

        // DTO 변환 및 반환
        return userAppliesPage.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserApplyResponseDTO> getUserAppliesByStatusPaged(Status status, Pageable pageable) {
        log.info("{} 사용자 상태별 매칭 신청 목록 페이징 조회: status={}, page={}, size={}",
                LOG_PREFIX, status, pageable.getPageNumber(), pageable.getPageSize());

        // 현재 사용자 정보 가져오기
        PetUser currentUser = getCurrentUser();

        // 상태별 페이징 처리된 쿼리 수행
        // (이 메서드는 UserApplyRepository에 추가해야 함)
        Page<UserApply> userAppliesPage = userApplyRepository.findByPetUser_UserIdAndStatusWithRelationsPaged(
                currentUser.getUserId(), status, pageable);

        // DTO 변환 및 반환
        return userAppliesPage.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserApplyResponseDTO> getTrainerAppliesByStatus(Status status) {
        log.info("{} 트레이너 상태별 매칭 신청 목록 조회: status={}", LOG_PREFIX, status);

        // 현재 트레이너 정보 가져오기
        PetUser currentUser = getCurrentUser();

        // 최적화된 쿼리로 트레이너의 상태별 신청 목록 조회
        List<UserApply> trainerApplies = userApplyRepository.findByTrainer_TrainerIdAndStatusWithRelations(
                currentUser.getUserId(), status);
        log.info("{} 조회된 신청 수: {}", LOG_PREFIX, trainerApplies.size());

        // DTO 변환 및 반환
        return trainerApplies.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserApplyResponseDTO> getTrainerAppliesPaged(Pageable pageable) {
        log.info("{} 트레이너 매칭 신청 목록 페이징 조회: page={}, size={}",
                LOG_PREFIX, pageable.getPageNumber(), pageable.getPageSize());

        // 현재 트레이너 정보 가져오기
        PetUser currentUser = getCurrentUser();

        // 페이징 처리된 쿼리로 트레이너의 신청 목록 조회
        Page<UserApply> trainerAppliesPage = userApplyRepository.findByTrainer_TrainerIdWithRelationsPaged(
                currentUser.getUserId(), pageable);

        // DTO 변환 및 반환
        return trainerAppliesPage.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserApplyResponseDTO> getTrainerAppliesByStatusPaged(Status status, Pageable pageable) {
        log.info("{} 트레이너 상태별 매칭 신청 목록 페이징 조회: status={}, page={}, size={}",
                LOG_PREFIX, status, pageable.getPageNumber(), pageable.getPageSize());

        // 현재 트레이너 정보 가져오기
        PetUser currentUser = getCurrentUser();

        // 상태별 페이징 처리된 쿼리 수행
        // (이 메서드는 UserApplyRepository에 추가해야 함)
        Page<UserApply> trainerAppliesPage = userApplyRepository.findByTrainer_TrainerIdAndStatusWithRelationsPaged(
                currentUser.getUserId(), status, pageable);

        // DTO 변환 및 반환
        return trainerAppliesPage.map(this::convertToResponseDTO);
    }

    private UUID getCurrentUserUUID() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomOAuth2User userDetails) {
            return userDetails.getUserId();
        }

        log.error("{} 인증 정보를 찾을 수 없음", LOG_PREFIX);
        throw ExceptionUtils.of(ErrorCode.UNAUTHORIZED);
    }

    private PetUser getCurrentUser() {
        UUID currentUserUUID = getCurrentUserUUID();
        return petUserRepository.findById(currentUserUUID)
                .orElseThrow(() -> ExceptionUtils.of(ErrorCode.USER_NOT_FOUND));
    }

    // UserApply 엔티티를 ResponseDTO로 변환하는 메서드
    public UserApplyResponseDTO convertToResponseDTO(UserApply userApply) {
        String createdAtStr = userApply.getCreatedAt() != null ?
                userApply.getCreatedAt().format(DATE_FORMATTER) : null;

        String updatedAtStr = userApply.getUpdatedAt() != null ?
                userApply.getUpdatedAt().format(DATE_FORMATTER) : null;

        // DTO 생성 및 반환
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
