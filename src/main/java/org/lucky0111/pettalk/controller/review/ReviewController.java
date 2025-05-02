package org.lucky0111.pettalk.controller.review;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.dto.review.*;
import org.lucky0111.pettalk.service.review.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer token"
)
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewResponseDTO> createReview(@RequestBody ReviewRequestDTO requestDTO, HttpServletRequest request) throws AccessDeniedException {
        log.info("리뷰 작성 요청: {}", requestDTO);
        ReviewResponseDTO responseDTO = reviewService.createReview(requestDTO, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    // 리뷰 목록 조회
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviews() {
        log.info("리뷰 목록 조회 요청");
        List<ReviewResponseDTO> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    // 리뷰 상세 조회
    @GetMapping("/{reviewId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable Long reviewId, HttpServletRequest request) {
        log.info("리뷰 상세 조회 요청: reviewId={}", reviewId);
        ReviewResponseDTO review = reviewService.getReviewById(reviewId, request);
        return ResponseEntity.ok(review);
    }

    // 리뷰 수정
    @PutMapping("/{reviewId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewUpdateDTO updateDTO,
            HttpServletRequest request) throws AccessDeniedException {
        log.info("리뷰 수정 요청: reviewId={}, updateDTO={}", reviewId, updateDTO);
        ReviewResponseDTO responseDTO = reviewService.updateReview(reviewId, updateDTO, request);
        return ResponseEntity.ok(responseDTO);
    }

    // 리뷰 삭제
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId, HttpServletRequest request) throws AccessDeniedException {
        log.info("리뷰 삭제 요청: reviewId={}", reviewId);
        reviewService.deleteReview(reviewId, request);
        return ResponseEntity.noContent().build();
    }

    // 훈련사 별 리뷰 목록 조회
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/trainers/{trainerId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByTrainerId(@PathVariable UUID trainerId, HttpServletRequest request) {
        log.info("훈련사 별 리뷰 목록 조회 요청: trainerId={}", trainerId);
        List<ReviewResponseDTO> reviews = reviewService.getReviewsByTrainerId(trainerId, request);
        return ResponseEntity.ok(reviews);
    }

    // 본인이 작성한 리뷰 리스트 조회
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users/me")
    public ResponseEntity<List<ReviewResponseDTO>> getMyReviews(HttpServletRequest request) {
        log.info("본인 작성 리뷰 목록 조회 요청");
        List<ReviewResponseDTO> reviews = reviewService.getMyReviews(request);
        return ResponseEntity.ok(reviews);
    }

    // 리뷰에 좋아요 추가
    @PostMapping("/{reviewId}/likes")
    public ResponseEntity<ReviewLikeResponseDTO> addLikeToReview(@PathVariable Long reviewId, HttpServletRequest request) {
        log.info("리뷰 좋아요 추가 요청: reviewId={}", reviewId);
        ReviewLikeResponseDTO likeResponse = reviewService.addLikeToReview(reviewId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(likeResponse);
    }

    // 리뷰에 좋아요 삭제
    @DeleteMapping("/{reviewId}/likes")
    public ResponseEntity<Void> removeLikeFromReview(@PathVariable Long reviewId, HttpServletRequest request) {
        log.info("리뷰 좋아요 삭제 요청: reviewId={}", reviewId);
        reviewService.removeLikeFromReview(reviewId, request);
        return ResponseEntity.noContent().build();
    }

    // 리뷰의 좋아요 개수 조회
    @GetMapping("/{reviewId}/likes/count")
    public ResponseEntity<ReviewLikeCountDTO> getReviewLikesCount(@PathVariable Long reviewId) {
        log.info("리뷰 좋아요 개수 조회 요청: reviewId={}", reviewId);
        ReviewLikeCountDTO likeCount = reviewService.getReviewLikesCount(reviewId);
        return ResponseEntity.ok(likeCount);
    }
}
