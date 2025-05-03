package org.lucky0111.pettalk.controller.match;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "매칭 신청", description = "사용자와 트레이너 간의 매칭 신청 관련 API")
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer token"
)
public class UserApplyController {

    private final UserApplyService userApplyService;


    @PostMapping
    @Operation(
            summary = "매칭 신청서 제출",
            description = "사용자가 트레이너에게 매칭 신청서를 제출합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserApplyResponseDTO> createApply(@RequestBody UserApplyRequestDTO requestDTO) {
        log.info("신청서 제출 요청: {}", requestDTO);
        UserApplyResponseDTO responseDTO = userApplyService.createApply(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(
            summary = "사용자 신청 목록 조회",
            description = "로그인한 사용자가 자신이 제출한 매칭 신청 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/user")
    public ResponseEntity<List<UserApplyResponseDTO>> getUserApplies() {
        log.info("사용자 신청 목록 조회 요청");
        List<UserApplyResponseDTO> responseDTOs = userApplyService.getUserApplies();
        return ResponseEntity.ok(responseDTOs);
    }

    @Operation(
            summary = "트레이너 신청 목록 조회",
            description = "트레이너가 자신에게 접수된 매칭 신청 목록을 조회합니다. 트레이너 또는 관리자 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/trainer")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    public ResponseEntity<List<UserApplyResponseDTO>> getTrainerApplies() {
        log.info("트레이너 신청 목록 조회 요청");
        List<UserApplyResponseDTO> responseDTOs = userApplyService.getTrainerApplies();
        return ResponseEntity.ok(responseDTOs);
    }

    @Operation(
            summary = "매칭 신청 상태 업데이트",
            description = "트레이너가 매칭 신청의 상태를 업데이트합니다(승인/거절 등). 트레이너 또는 관리자 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
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

        UserApplyResponseDTO responseDTO = userApplyService.updateApplyStatus(applyId, status);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(
            summary = "매칭 신청 삭제",
            description = "사용자가 자신이 제출한 매칭 신청을 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{applyId}/delete")
    public ResponseEntity<UserApplyResponseDTO> deleteApply(
            @PathVariable Long applyId
            ) {

        log.info("신청 삭제 요청: applyId={}", applyId);

        UserApplyResponseDTO responseDTO = userApplyService.deleteApply(applyId);
        return ResponseEntity.ok(responseDTO);
    }
}
