package org.lucky0111.pettalk.controller.trainer;

import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.dto.trainer.TrainerApplicationRequestDTO;
import org.lucky0111.pettalk.domain.dto.trainer.TrainerDTO;
import org.lucky0111.pettalk.service.trainer.TrainerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
public class TrainerController {
    private final TrainerService trainerService;

    @GetMapping("/{trainerId}")
    public ResponseEntity<TrainerDTO> getTrainer(@PathVariable UUID trainerId) {
        TrainerDTO dto = trainerService.getTrainerDetails(trainerId);
        return ResponseEntity.ok().body(dto);
    }
    @PostMapping("/apply")
    // @RequestPart 어노테이션으로 JSON 데이터와 파일 목록을 분리하여 받음
    public ResponseEntity<Void> applyForTrainer(
            @RequestPart("request") TrainerApplicationRequestDTO applicationRequest, // 신청 정보 JSON
            @RequestPart("files") List<MultipartFile> certificationFiles, // 자격증 파일 목록
            @RequestHeader("X-User-ID") UUID userId // 헤더 등에서 사용자 ID를 가져온다고 가정 (인증 로직 필요)
            // Spring Security 사용 시 Principal 객체 등에서 사용자 정보를 안전하게 가져와야 합니다.
    ) {
        // Service 레이어 호출
        trainerService.applyTrainer(userId, applicationRequest, certificationFiles);

        // 신청 성공 응답 (예: 202 Accepted)
        return ResponseEntity.accepted().build();
    }
}

