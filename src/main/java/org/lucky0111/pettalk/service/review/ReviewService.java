package org.lucky0111.pettalk.service.review;

import org.lucky0111.pettalk.domain.dto.review.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ReviewService {
    ReviewResponseDTO createReview(ReviewRequestDTO requestDTO);

    List<ReviewResponseDTO> getAllReviews();

    ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateDTO updateDTO);

    void deleteReview(Long reviewId);

    List<ReviewResponseDTO> getReviewsByTrainerNickname(String trainerNickname);

    List<ReviewResponseDTO> getMyReviews();

    List<ReviewResponseDTO> getMyTrainerReviews();

    ResponseEntity<?> toggleLikeForReview(Long reviewId);

    List<ReviewResponseDTO> getTopLikedReviews(int limit);

}
