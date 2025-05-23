package org.lucky0111.pettalk.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.common.UserRole;
import org.lucky0111.pettalk.domain.dto.admin.AdminCertificationDTO;
import org.lucky0111.pettalk.domain.dto.admin.AdminCommentDTO;
import org.lucky0111.pettalk.domain.dto.admin.AdminPostDTO;
import org.lucky0111.pettalk.domain.dto.admin.AdminUserDTO;
import org.lucky0111.pettalk.domain.dto.review.ReviewResponseDTO;
import org.lucky0111.pettalk.service.admin.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAuthority('ADMIN')")
@Validated
@Tag(name = "관리자 API", description = "관리자 관련 API")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "인증 요청 목록 조회", description = "인증 요청 목록을 조회합니다.")
    @GetMapping("/certifications")
    public ResponseEntity<List<AdminCertificationDTO>> getCertificationList() {

        log.info("getCertificationList 호출됨");
        return ResponseEntity.ok(adminService.getAllCertificationsForAdmin());
    }

    @Operation(summary = "인증 요청 승인", description = "인증 요청을 승인합니다.")
    @PutMapping("/certifications/approve/{id}")
    public ResponseEntity<Void> approveCertificationStatus(
            @PathVariable("id")
            @Positive(message = "인증서 ID는 양수여야 합니다")
            Long certId
    ) {

        log.info("approveCertificationStatus 호출됨: certId={}", certId);
        adminService.approveCertificationForAdmin(certId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "인증 요청 거부", description = "인증 요청을 거부합니다.")
    @PutMapping("/certifications/reject/{id}")
    public ResponseEntity<Void> rejectCertificationStatus(
            @PathVariable("id")
            @Positive(message = "인증서 ID는 양수여야 합니다")
            Long certId
    ) {

        log.info("rejectCertificationStatus 호출됨: certId={}", certId);
        adminService.rejectCertificationForAdmin(certId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 목록 조회", description = "사용자 목록을 조회합니다.")
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDTO>> getUserList() {

        log.info("getUserList 호출됨");
        return ResponseEntity.ok(adminService.getAllUsersForAdmin());
    }

    @Operation(summary = "사용자 역할 및 상태 변경", description = "사용자의 역할과 상태를 변경합니다.")
    @PutMapping("/users/update/{id}")
    public ResponseEntity<Void> updateRoleAndStatus(
            @PathVariable("id")
            UUID userId,

            @RequestParam("role")
            UserRole role,

            @RequestParam("status")
            @NotBlank(message = "상태는 필수입니다")
            @Pattern(regexp = "^(ACTIVE|INACTIVE|WITHDRAWN)$",
                    message = "상태는 ACTIVE, INACTIVE, WITHDRAWN 중 하나여야 합니다")
            String status
    ) {

        log.info("updateRoleAndStatus 호출됨: userId={}, role={}, status={}", userId, role, status);
        adminService.updateUserRoleAndStatusForAdmin(userId, role, status);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 탈퇴", description = "사용자를 탈퇴 처리합니다.")
    @PutMapping("/users/withdraw/{id}")
    public ResponseEntity<Void> withdrawUser(
            @PathVariable("id")
            UUID userId
    ) {

        log.info("withdrawUser 호출됨: userId={}", userId);
        adminService.withdrawUserForAdmin(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "리뷰 목록 조회", description = "리뷰 목록을 조회합니다.")
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewList() {

        log.info("getReviewList 호출됨");
        return ResponseEntity.ok(adminService.getAllReviewsForAdmin());
    }

    @Operation(summary = "리뷰 삭제", description = "리뷰를 삭제합니다.")
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable("id")
            @Positive(message = "리뷰 ID는 양수여야 합니다")
            Long reviewId
    ) {

        log.info("deleteReview 호출됨: reviewId={}", reviewId);
        adminService.deleteReviewForAdmin(reviewId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "게시물 목록 조회", description = "게시물 목록을 조회합니다.")
    @GetMapping("/posts")
    public ResponseEntity<List<AdminPostDTO>> getPostList() {

        log.info("getPostList 호출됨");
        return ResponseEntity.ok(adminService.getAllPostsForAdmin());
    }

    @Operation(summary = "게시물 삭제", description = "게시물을 삭제합니다.")
    @DeleteMapping("/posts/delete/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable("id")
            @Positive(message = "게시글 ID는 양수여야 합니다")
            Long postId
    ) {

        log.info("deletePost 호출됨: postId={}", postId);
        adminService.deletePostForAdmin(postId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 목록 조회", description = "댓글 목록을 조회합니다.")
    @GetMapping("/comments")
    public ResponseEntity<List<AdminCommentDTO>> getCommentList() {

        log.info("getCommentList 호출됨");
        return ResponseEntity.ok(adminService.getAllCommentsForAdmin());
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @PutMapping("/comments/delete/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("id")
            @Positive(message = "댓글 ID는 양수여야 합니다")
            Long commentId
    ) {

        log.info("deleteComment 호출됨: commentId={}", commentId);
        adminService.deleteCommentForAdmin(commentId);
        return ResponseEntity.ok().build();
    }

}
