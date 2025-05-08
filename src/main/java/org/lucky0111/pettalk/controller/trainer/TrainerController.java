package org.lucky0111.pettalk.controller.trainer;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.dto.trainer.CertificationRequestDTO;
import org.lucky0111.pettalk.domain.dto.trainer.TrainerDTO;
import org.lucky0111.pettalk.domain.dto.trainer.TrainerPageDTO;
import org.lucky0111.pettalk.service.trainer.TrainerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
public class TrainerController {
    private final TrainerService trainerService;

    private static final int PAGE_SIZE = 5;

    @GetMapping
    @Operation(summary = "트레이너 목록 조회", description = "트레이너 목록을 페이지 단위로 조회합니다.")
    public ResponseEntity<TrainerPageDTO> getAllTrainers(
            @RequestParam(defaultValue = "0") int page
    ) {
        TrainerPageDTO posts = trainerService.getAllTrainers(page, PAGE_SIZE);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{trainerNickname}") // 조회만 Nickname을 이용해서.
    @Operation(summary = "트레이너 전체 내용 조회", description = "Nickname으로 전체 내용을 조회합니다.")
    public ResponseEntity<TrainerDTO> getTrainer(@PathVariable String trainerNickname) {
        TrainerDTO dto = trainerService.getTrainerDetails(trainerNickname);
        return ResponseEntity.ok().body(dto);
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

}

