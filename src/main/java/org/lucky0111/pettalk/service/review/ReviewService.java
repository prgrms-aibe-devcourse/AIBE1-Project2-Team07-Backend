package org.lucky0111.pettalk.service.review;

import org.lucky0111.pettalk.domain.dto.review.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ReviewService {
    ReviewResponseDTO createReview(ReviewRequestDTO requestDTO, MultipartFile file) throws IOException;

    List<ReviewResponseDTO> getAllReviews();

    ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateDTO updateDTO);

    void deleteReview(Long reviewId);

    List<ReviewResponseDTO> getReviewsByTrainerNickname(String trainerNickname);

    List<ReviewResponseDTO> getMyReviews();

    List<ReviewResponseDTO> getMyTrainerReviews();

    ResponseEntity<?> toggleLikeForReview(Long reviewId);

    List<ReviewResponseDTO> getTopLikedReviews(int limit);

    ReviewResponseDTO getReviewByApplyId(Long applyId);
}
