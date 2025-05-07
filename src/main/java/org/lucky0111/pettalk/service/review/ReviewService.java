package org.lucky0111.pettalk.service.review;

import jakarta.servlet.http.HttpServletRequest;
import org.lucky0111.pettalk.domain.dto.review.*;
import org.springframework.http.ResponseEntity;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

public interface ReviewService {
    ReviewResponseDTO createReview(ReviewRequestDTO requestDTO);

    List<ReviewResponseDTO> getAllReviews();

    ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateDTO updateDTO);

    void deleteReview(Long reviewId);

    List<ReviewResponseDTO> getReviewsByTrainerName(String trainerName);

    List<ReviewResponseDTO> getMyReviews();

    List<ReviewResponseDTO> getMyTrainerReviews();

    ResponseEntity<?> toggleLikeForReview(Long reviewId);

}
