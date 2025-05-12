package org.lucky0111.pettalk.controller.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.dto.review.*;
import org.lucky0111.pettalk.service.review.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "리뷰 관리", description = "리뷰 CRUD 및 좋아요 기능 API")
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer token"
)
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(
            summary = "리뷰 작성",
            description = "새로운 리뷰를 작성합니다. 인증된 사용자만 접근 가능합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @RequestPart ReviewRequestDTO requestDTO,
            @RequestPart MultipartFile file
            ) throws IOException {
        log.info("리뷰 작성 요청: {}", requestDTO);
        ReviewResponseDTO responseDTO = reviewService.createReview(requestDTO, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    @Operation(
            summary = "전체 리뷰 목록 조회",
            description = "모든 리뷰 목록을 조회합니다. 인증된 사용자만 접근 가능합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviews() {
        log.info("리뷰 목록 조회 요청");
        List<ReviewResponseDTO> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{reviewId}")
    @Operation(
            summary = "리뷰 수정",
            description = "특정 ID의 리뷰를 수정합니다. 작성자 또는 관리자만 수정 가능합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewUpdateDTO updateDTO
            ) {
        log.info("리뷰 수정 요청: reviewId={}, updateDTO={}", reviewId, updateDTO);
        ReviewResponseDTO responseDTO = reviewService.updateReview(reviewId, updateDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "리뷰 삭제",
            description = "특정 ID의 리뷰를 삭제합니다. 작성자 또는 관리자만 삭제 가능합니다."
    )
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        log.info("리뷰 삭제 요청: reviewId={}", reviewId);
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "훈련사 별 리뷰 목록 조회",
            description = "특정 훈련사의 리뷰 목록을 조회합니다. 인증된 사용자만 접근 가능합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/trainers/{trainerNickname}/open")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByTrainerId(@PathVariable String trainerNickname) {
        log.info("훈련사 별 리뷰 목록 조회 요청: trainerNickname={}", trainerNickname);
        List<ReviewResponseDTO> reviews = reviewService.getReviewsByTrainerNickname(trainerNickname);
        return ResponseEntity.ok(reviews);
    }

    @Operation(
            summary = "내가 작성한 리뷰 목록 조회",
            description = "현재 로그인한 사용자가 작성한 리뷰 목록을 조회합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users/me")
    public ResponseEntity<List<ReviewResponseDTO>> getMyReviews() {
        log.info("본인 작성 리뷰 목록 조회 요청");
        List<ReviewResponseDTO> reviews = reviewService.getMyReviews();
        return ResponseEntity.ok(reviews);
    }

    @Operation(
            summary = "내가 받은 리뷰 목록 조회",
            description = "현재 로그인한 사용자가 받은 리뷰 목록을 조회합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users/trainer")
    public ResponseEntity<List<ReviewResponseDTO>> getMyTrainerReviews() {
        log.info("본인 받은 리뷰 목록 조회 요청");
        List<ReviewResponseDTO> reviews = reviewService.getMyTrainerReviews();
        return ResponseEntity.ok(reviews);
    }

    @Operation(
            summary = "리뷰 좋아요 토글",
            description = "특정 리뷰에 좋아요를 토글(추가/삭제)합니다. 인증된 사용자만 가능합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{reviewId}/likes/toggle")
    public ResponseEntity<?> toggleReviewLike(@PathVariable Long reviewId) {
        log.info("리뷰 좋아요 토글 요청: reviewId={}", reviewId);
        return reviewService.toggleLikeForReview(reviewId);
    }

    @Operation(
            summary = "좋아요 상위 리뷰 목록 조회",
            description = "좋아요 개수가 가장 많은 리뷰를 상위 9개까지 조회합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/top-liked/open")
    public ResponseEntity<List<ReviewResponseDTO>> getTopLikedReviews(
            @RequestParam(defaultValue = "9") int limit) {
        log.info("좋아요 상위 리뷰 목록 조회 요청: limit={}", limit);
        List<ReviewResponseDTO> reviews = reviewService.getTopLikedReviews(limit);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("applyId/{applyId}")
    @Operation(
            summary = "applyId로 리뷰 목록 조회",
            description = "applyId로 리뷰 목록을 조회합니다. 인증된 사용자만 접근 가능합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewResponseDTO> getReviewByApplyId(@PathVariable Long applyId) {
        log.info("applyId로 목록 조회 요청");
        ReviewResponseDTO reviews = reviewService.getReviewByApplyId(applyId);
        return ResponseEntity.ok(reviews);
    }

}
