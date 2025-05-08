package org.lucky0111.pettalk.controller.trainer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.dto.trainer.TrainerApplicationRequestDTO;
import org.lucky0111.pettalk.domain.dto.trainer.TrainerDTO;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
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

    @GetMapping("/{trainerNickname}")
    public ResponseEntity<TrainerDTO> getTrainer(@PathVariable String trainerNickname) {
        TrainerDTO dto = trainerService.getTrainerDetails(trainerNickname);
        return ResponseEntity.ok().body(dto);
    }

    @PostMapping("/apply")
    // @RequestPart 어노테이션으로 JSON 데이터와 파일 목록을 분리하여 받음
    public ResponseEntity<Void> applyForTrainer(
            @RequestPart("request") TrainerApplicationRequestDTO applicationRequest, // 신청 정보 JSON
            @RequestPart("files") List<MultipartFile> certificationFiles,
            @RequestHeader("X-User-ID") UUID userId
    ) {
        trainerService.applyTrainer(userId, applicationRequest, certificationFiles);

        return ResponseEntity.accepted().build();
    }
}

