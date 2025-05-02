package org.lucky0111.pettalk.controller.match;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.common.Status;
import org.lucky0111.pettalk.domain.dto.match.UserApplyRequestDTO;
import org.lucky0111.pettalk.domain.dto.match.UserApplyResponseDTO;
import org.lucky0111.pettalk.service.match.UserApplyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/match")
@RequiredArgsConstructor
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer token"
)
public class UserApplyController {

    private final UserApplyService userApplyService;

    // 신청서 제출 API
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserApplyResponseDTO> createApply(@RequestBody UserApplyRequestDTO requestDTO, HttpServletRequest request) {
        log.info("신청서 제출 요청: {}", requestDTO);
        UserApplyResponseDTO responseDTO = userApplyService.createApply(requestDTO, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    // 사용자 본인의 신청 목록 조회 API
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/user")
    public ResponseEntity<List<UserApplyResponseDTO>> getUserApplies(HttpServletRequest request) {
        log.info("사용자 신청 목록 조회 요청");
        List<UserApplyResponseDTO> responseDTOs = userApplyService.getUserApplies(request);
        return ResponseEntity.ok(responseDTOs);
    }

    // 트레이너에게 온 신청 목록 조회 API
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/trainer")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    public ResponseEntity<List<UserApplyResponseDTO>> getTrainerApplies(HttpServletRequest request) {
        log.info("트레이너 신청 목록 조회 요청");
        List<UserApplyResponseDTO> responseDTOs = userApplyService.getTrainerApplies(request);
        return ResponseEntity.ok(responseDTOs);
    }

    // 신청 상태 업데이트 API (트레이너만 가능)
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{applyId}/status")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    public ResponseEntity<UserApplyResponseDTO> updateApplyStatus(
            @PathVariable Long applyId,
            @RequestBody Map<String, String> statusRequest,
            HttpServletRequest request) {

        log.info("신청 상태 업데이트 요청: applyId={}, status={}", applyId, statusRequest.get("status"));

        Status status;
        try {
            status = Status.valueOf(statusRequest.get("status").toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        UserApplyResponseDTO responseDTO = userApplyService.updateApplyStatus(applyId, status, request);
        return ResponseEntity.ok(responseDTO);
    }

    // 신청 삭제 업데이트 API (유저만 가능)
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{applyId}/delete")
    public ResponseEntity<UserApplyResponseDTO> deleteApply(
            @PathVariable Long applyId,
            HttpServletRequest request) {

        log.info("신청 삭제 요청: applyId={}", applyId);

        UserApplyResponseDTO responseDTO = userApplyService.deleteApply(applyId, request);
        return ResponseEntity.ok(responseDTO);
    }
}
