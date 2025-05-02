package org.lucky0111.pettalk.service.match;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.common.Status;
import org.lucky0111.pettalk.domain.dto.match.UserApplyRequestDTO;
import org.lucky0111.pettalk.domain.dto.match.UserApplyResponseDTO;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.domain.entity.trainer.Trainer;
import org.lucky0111.pettalk.domain.entity.match.UserApply;
import org.lucky0111.pettalk.repository.match.UserApplyRepository;
import org.lucky0111.pettalk.repository.trainer.TrainerRepository;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.lucky0111.pettalk.util.auth.JWTUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
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
    public UserApplyResponseDTO createApply(UserApplyRequestDTO requestDTO, HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);
        PetUser currentUser = getCurrentUser(request);

        // 트레이너 조회
        Trainer trainer = trainerRepository.findById(requestDTO.trainerId())
                .orElseThrow(() -> new IllegalArgumentException("트레이너를 찾을 수 없습니다."));

        if (userApplyRepository.existsByPetUser_userIdAndTrainer_trainerIdAndStatus(
                currentUserUUID,
                requestDTO.trainerId(),
                Status.PENDING
        )) {
            throw new IllegalArgumentException("현재 신청 중 입니다.");
        };

        // UserApply 엔티티 생성
        UserApply userApply = new UserApply();
        userApply.setPetUser(currentUser);
        userApply.setTrainer(trainer);
        userApply.setContent(requestDTO.content());
        userApply.setImageUrl(requestDTO.imageUrl());
        userApply.setStatus(Status.PENDING);

        // 저장
        UserApply savedApply = userApplyRepository.save(userApply);

        // 응답 DTO 변환 및 반환
        return convertToResponseDTO(savedApply);
    }

    @Transactional(readOnly = true)
    public List<UserApplyResponseDTO> getUserApplies(HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);
        PetUser currentUser = getCurrentUser(request);

        List<UserApply> userApplies = userApplyRepository.findByPetUser_UserId(currentUserUUID);

        // 응답 DTO 변환 및 반환
        return userApplies.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserApplyResponseDTO> getTrainerApplies(HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);
        PetUser currentUser = getCurrentUser(request);


        List<UserApply> trainerApplies = userApplyRepository.findByTrainer_TrainerId(currentUserUUID);

        // 응답 DTO 변환 및 반환
        return trainerApplies.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserApplyResponseDTO updateApplyStatus(Long applyId, Status status, HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);
        PetUser currentUser = getCurrentUser(request);

        // 신청 정보 조회
        UserApply userApply = userApplyRepository.findById(applyId)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보를 찾을 수 없습니다."));

        // 트레이너 권한 확인
        if (!userApply.getTrainer().getTrainerId().equals(currentUserUUID)) {
            throw new IllegalArgumentException("해당 신청에 대한 권한이 없습니다.");
        }

        // 상태 업데이트
        userApply.setStatus(status);

        // 저장
        UserApply updatedApply = userApplyRepository.save(userApply);

        // 응답 DTO 변환 및 반환
        return convertToResponseDTO(updatedApply);
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
                userApply.getContent(),
                userApply.getImageUrl(),
                userApply.getStatus(),
                createdAtStr,
                updatedAtStr
        );
    }

    public UserApplyResponseDTO deleteApply(Long applyId, HttpServletRequest request) throws AccessDeniedException {
        UUID currentUserUUID = getCurrentUserUUID(request);
        PetUser currentUser = getCurrentUser(request);

        UserApply userApply = userApplyRepository.findById(applyId)
                .orElseThrow(() -> new EntityNotFoundException("해당 신청서를 찾을 수 없습니다: " + applyId));

        // 현재 사용자가 신청서의 작성자인지 확인
        if (!userApply.getPetUser().getUserId().equals(currentUserUUID)) {
            throw new AccessDeniedException("해당 신청서를 삭제할 권한이 없습니다.");
        }

        // 삭제 전에 응답용 DTO 생성
        UserApplyResponseDTO responseDTO = convertToResponseDTO(userApply);

        // 신청서 삭제
        userApplyRepository.delete(userApply);

        return responseDTO;
    }

    private String extractJwtToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        System.out.println("bearerToken = " + bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {

            return bearerToken.substring(7);
        }
        return null;
    }

    // 현재 인증된 사용자의 UUID 가져오기
    private UUID getCurrentUserUUID(HttpServletRequest request) {
        String token = extractJwtToken(request);
        if (token == null) {
            throw new RuntimeException("인증 토큰을 찾을 수 없습니다.");
        }
        return jwtUtil.getUserId(token);
    }

    // 현재 사용자 엔티티 가져오기
    private PetUser getCurrentUser(HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);
        return petUserRepository.findById(currentUserUUID)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }


}
