package org.lucky0111.pettalk.service.review;

import org.lucky0111.pettalk.domain.dto.review.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

public interface ReviewService {
    // 리뷰 작성
    ReviewResponseDTO createReview(ReviewRequestDTO requestDTO) throws AccessDeniedException;

    // 리뷰 목록 조회
    List<ReviewResponseDTO> getAllReviews();

    // 리뷰 상세 조회
    ReviewResponseDTO getReviewById(Long reviewId);

    // 리뷰 수정
    ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateDTO updateDTO) throws AccessDeniedException;

    // 리뷰 삭제
    void deleteReview(Long reviewId) throws AccessDeniedException;

    // 훈련사 별 리뷰 목록 조회
    List<ReviewResponseDTO> getReviewsByTrainerId(UUID trainerId);

    // 본인이 작성한 리뷰 리스트 조회
    List<ReviewResponseDTO> getMyReviews();

    // 리뷰에 좋아요 추가
    ReviewLikeResponseDTO addLikeToReview(Long reviewId);

    // 리뷰에 좋아요 삭제
    void removeLikeFromReview(Long reviewId);

    // 리뷰의 좋아요 개수 조회
    ReviewLikeCountDTO getReviewLikesCount(Long reviewId);
}
