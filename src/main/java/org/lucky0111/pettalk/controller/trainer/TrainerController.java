package org.lucky0111.pettalk.controller.trainer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.dto.trainer.CertificationRequestDTO;
import org.lucky0111.pettalk.domain.dto.trainer.TrainerDTO;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
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

    @GetMapping("/{trainerNickname}") // 조회만 Nickname을 이용해서.
    public ResponseEntity<TrainerDTO> getTrainer(@PathVariable String trainerNickname) {
        TrainerDTO dto = trainerService.getTrainerDetails(trainerNickname);
        return ResponseEntity.ok().body(dto);
    }

    @PostMapping("/apply")
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
    public ResponseEntity<Void> addTrainerCertification(
            @PathVariable UUID trainerId, // 경로 변수에서 트레이너 ID 수신 (신청 시작 시 부여받은/생성된 ID)
            @RequestPart("certification") @Valid CertificationRequestDTO certificationDTO,
            @RequestPart("file") MultipartFile certificationFile
    ) {
        trainerService.addCertification(trainerId, certificationDTO, certificationFile);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}

