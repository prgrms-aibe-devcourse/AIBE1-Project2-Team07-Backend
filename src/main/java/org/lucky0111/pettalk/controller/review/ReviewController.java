package org.lucky0111.pettalk.controller.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.dto.review.*;
import org.lucky0111.pettalk.service.review.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<ReviewResponseDTO> createReview(@RequestBody ReviewRequestDTO requestDTO, HttpServletRequest request){
        log.info("리뷰 작성 요청: {}", requestDTO);
        ReviewResponseDTO responseDTO = reviewService.createReview(requestDTO, request);
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

    @GetMapping("/{reviewId}")
    @Operation(
            summary = "리뷰 상세 조회",
            description = "특정 ID의 리뷰를 상세 조회합니다. 인증된 사용자만 접근 가능합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable Long reviewId, HttpServletRequest request) {
        log.info("리뷰 상세 조회 요청: reviewId={}", reviewId);
        ReviewResponseDTO review = reviewService.getReviewById(reviewId, request);
        return ResponseEntity.ok(review);
    }

    @PutMapping("/{reviewId}")
    @Operation(
            summary = "리뷰 수정",
            description = "특정 ID의 리뷰를 수정합니다. 작성자 또는 관리자만 수정 가능합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewUpdateDTO updateDTO,
            HttpServletRequest request) {
        log.info("리뷰 수정 요청: reviewId={}, updateDTO={}", reviewId, updateDTO);
        ReviewResponseDTO responseDTO = reviewService.updateReview(reviewId, updateDTO, request);
        return ResponseEntity.ok(responseDTO);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "리뷰 삭제",
            description = "특정 ID의 리뷰를 삭제합니다. 작성자 또는 관리자만 삭제 가능합니다."
    )
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId, HttpServletRequest request) {
        log.info("리뷰 삭제 요청: reviewId={}", reviewId);
        reviewService.deleteReview(reviewId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "훈련사 별 리뷰 목록 조회",
            description = "특정 훈련사의 리뷰 목록을 조회합니다. 인증된 사용자만 접근 가능합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/trainers/{trainerId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByTrainerId(@PathVariable UUID trainerId, HttpServletRequest request) {
        log.info("훈련사 별 리뷰 목록 조회 요청: trainerId={}", trainerId);
        List<ReviewResponseDTO> reviews = reviewService.getReviewsByTrainerId(trainerId, request);
        return ResponseEntity.ok(reviews);
    }

    @Operation(
            summary = "내가 작성한 리뷰 목록 조회",
            description = "현재 로그인한 사용자가 작성한 리뷰 목록을 조회합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users/me")
    public ResponseEntity<List<ReviewResponseDTO>> getMyReviews(HttpServletRequest request) {
        log.info("본인 작성 리뷰 목록 조회 요청");
        List<ReviewResponseDTO> reviews = reviewService.getMyReviews(request);
        return ResponseEntity.ok(reviews);
    }

    @Operation(
            summary = "리뷰 좋아요 추가",
            description = "특정 리뷰에 좋아요를 추가합니다. 인증된 사용자만 가능합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{reviewId}/likes")
    public ResponseEntity<ReviewLikeResponseDTO> addLikeToReview(@PathVariable Long reviewId, HttpServletRequest request) {
        log.info("리뷰 좋아요 추가 요청: reviewId={}", reviewId);
        ReviewLikeResponseDTO likeResponse = reviewService.addLikeToReview(reviewId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(likeResponse);
    }

    @Operation(
            summary = "리뷰 좋아요 삭제",
            description = "특정 리뷰에 좋아요를 삭제합니다. 인증된 사용자만 가능합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{reviewId}/likes")
    public ResponseEntity<Void> removeLikeFromReview(@PathVariable Long reviewId, HttpServletRequest request) {
        log.info("리뷰 좋아요 삭제 요청: reviewId={}", reviewId);
        reviewService.removeLikeFromReview(reviewId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "리뷰 좋아요 개수 조회",
            description = "특정 리뷰의 좋아요 개수를 조회합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{reviewId}/likes/count")
    public ResponseEntity<ReviewLikeCountDTO> getReviewLikesCount(@PathVariable Long reviewId) {
        log.info("리뷰 좋아요 개수 조회 요청: reviewId={}", reviewId);
        ReviewLikeCountDTO likeCount = reviewService.getReviewLikesCount(reviewId);
        return ResponseEntity.ok(likeCount);
    }
}
