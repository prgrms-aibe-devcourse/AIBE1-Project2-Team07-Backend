package org.lucky0111.pettalk.controller.trainer;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.common.TrainerSearchType;
import org.lucky0111.pettalk.domain.common.TrainerSortType;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.dto.trainer.CertificationRequestDTO;
import org.lucky0111.pettalk.domain.dto.trainer.TrainerDTO;
import org.lucky0111.pettalk.domain.dto.trainer.TrainerPageDTO;
import org.lucky0111.pettalk.domain.dto.trainer.TrainerProfileUpdateDTO;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.exception.CustomException;
import org.lucky0111.pettalk.service.trainer.TrainerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
public class TrainerController {
    private final TrainerService trainerService;

    private static final int PAGE_SIZE = 5;

    @GetMapping("/open")
    @Operation(summary = "트레이너 목록 조회", description = "트레이너 목록을 페이지 단위로 조회합니다.")
    public ResponseEntity<TrainerPageDTO> getAllTrainers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ALL") TrainerSearchType searchType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "LATEST")TrainerSortType sortType
            ) {
        TrainerPageDTO posts = trainerService.searchTrainers(keyword, searchType, page, PAGE_SIZE, sortType);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{trainerNickname}/open") // 조회만 Nickname을 이용해서.
    @Operation(summary = "트레이너 전체 내용 조회", description = "Nickname으로 전체 내용을 조회합니다.")
    public ResponseEntity<TrainerDTO> getTrainer(@PathVariable String trainerNickname) {
        TrainerDTO dto = trainerService.getTrainerDetails(trainerNickname);
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping("/random/open") // 조회만 Nickname을 이용해서.
    @Operation(summary = "트레이너 전체 내용 조회", description = "Nickname으로 전체 내용을 조회합니다.")
    public ResponseEntity<List<TrainerDTO>> getRandomTrainer() {
        List<TrainerDTO> dtos = trainerService.getRandomTrainers();
        return ResponseEntity.ok().body(dtos);
    }

    @PostMapping("/apply")
    @Operation(summary = "트레이너 승급 신청", description = "승급 신청 시 자격증 정보 필요하며, 파일은 multipart/form-data 필요")
    public ResponseEntity<Void> applyForTrainer(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @RequestPart("certification") @Valid CertificationRequestDTO certificationDTO,
            @RequestPart("file") MultipartFile certificationFile
    ) {
        UUID userId = principal.getUserId();
        trainerService.applyTrainer(userId, certificationDTO, certificationFile);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{trainerId}/certifications")
    @Operation(summary = "트레이너 자격증 추가", description = "승급 된 이후 자격증을 더 추가할때 사용하며, 파일은 multipart/form-data 필요")
    public ResponseEntity<Void> addTrainerCertification(
            @PathVariable UUID trainerId, // 경로 변수에서 트레이너 ID 수신 (신청 시작 시 부여받은/생성된 ID)
            @RequestPart("certification") @Valid CertificationRequestDTO certificationDTO,
            @RequestPart("file") MultipartFile certificationFile
    ) {
        trainerService.addCertification(trainerId, certificationDTO, certificationFile);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{trainerId}")
    @Operation(summary = "트레이너 프로필 수정", description = "모든 항목 기입해야하며 사진은 필수2장, 이미 DB에 사진이 들어가있어도 삭제후 재 등록하는 형태 ")
    public ResponseEntity<Void> updateTrainerProfile(
            @AuthenticationPrincipal CustomOAuth2User principal, // principal은 CustomOAuth2User 타입
            @PathVariable UUID trainerId,
            @RequestPart("profileData") @Valid TrainerProfileUpdateDTO updateDTO,
            @RequestPart("photos") List<MultipartFile> photos
    ) {
        UUID authenticatedUserId = principal.getUserId();
        if (!authenticatedUserId.equals(trainerId)) {
            throw new CustomException("자신의 프로필만 수정할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        // 서비스 메소드 호출 (인증된 사용자 ID와 트레이너 ID가 일치함을 검증 후 호출)
        trainerService.updateTrainerProfile(authenticatedUserId, trainerId, updateDTO, photos);


        // 업데이트 성공 응답 반환 (응답 본문 없음)
        return ResponseEntity.status(HttpStatus.OK).build(); // 200 OK 반환
    }

}

