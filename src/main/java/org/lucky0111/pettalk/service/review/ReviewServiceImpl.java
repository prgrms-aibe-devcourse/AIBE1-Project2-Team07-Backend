package org.lucky0111.pettalk.service.review;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.common.Status;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.dto.review.*;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.domain.entity.match.UserApply;
import org.lucky0111.pettalk.domain.entity.review.Review;
import org.lucky0111.pettalk.domain.entity.review.ReviewLike;
import org.lucky0111.pettalk.exception.CustomException;
import org.lucky0111.pettalk.repository.match.UserApplyRepository;
import org.lucky0111.pettalk.repository.review.ReviewLikeRepository;
import org.lucky0111.pettalk.repository.review.ReviewRepository;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.lucky0111.pettalk.util.auth.JWTUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserApplyRepository userApplyRepository;
    private final PetUserRepository petUserRepository;
    private final JWTUtil jwtUtil;

    @Override
    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO requestDTO) {
        PetUser currentUser = getCurrentUser();

        UserApply userApply = userApplyRepository.findById(requestDTO.applyId())
                .orElseThrow(() -> new CustomException("해당 신청서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (!userApply.getPetUser().getUserId().equals(currentUser.getUserId())) {
            throw new CustomException("리뷰를 작성할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        if (userApply.getStatus() != Status.APPROVED) {
            throw new CustomException("승인된 신청에 대해서만 리뷰를 작성할 수 있습니다.", HttpStatus.BAD_REQUEST);
        }

        // 이미 리뷰가 존재하는지 확인
        if (reviewRepository.existsByUserApply(userApply)) {
            throw new CustomException("이미 리뷰가 존재합니다.", HttpStatus.CONFLICT);
        }

        // 리뷰 생성
        Review review = new Review();
        review.setUserApply(userApply);
        review.setRating(requestDTO.rating());
        review.setTitle(requestDTO.title());
        review.setComment(requestDTO.comment());
        review.setReviewImageUrl(requestDTO.reviewImageUrl());

        Review savedReview = reviewRepository.save(review);

        return convertToResponseDTO(savedReview, currentUser.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getAllReviews() {
        UUID currentUserUUID = getCurrentUserUUID();

        List<Review> reviews = reviewRepository.findAllWithRelations();

        Map<Long, Integer> likeCounts = reviewLikeRepository.countLikesByReviewIds(
                        reviews.stream().map(Review::getReviewId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(
                        ReviewLikeRepository.ReviewLikeCountProjection::getReviewId,
                        ReviewLikeRepository.ReviewLikeCountProjection::getLikeCount
                ));

// 사용자의 좋아요 여부를 한 번에 조회
        Map<Long, Boolean> userLikedMap = reviewLikeRepository.checkUserLikeStatus(
                        reviews.stream().map(Review::getReviewId).collect(Collectors.toList()),
                        currentUserUUID)
                .stream()
                .collect(Collectors.toMap(
                        ReviewLikeRepository.ReviewLikeStatusProjection::getReviewId,
                        ReviewLikeRepository.ReviewLikeStatusProjection::getHasLiked
                ));

        return reviews.stream()
                .map(review -> convertToResponseDTO(
                        review,
                        currentUserUUID,
                        likeCounts.getOrDefault(review.getReviewId(), 0),
                        userLikedMap.getOrDefault(review.getReviewId(), false)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReviewById(Long reviewId) {
        UUID currentUserUUID = getCurrentUserUUID();

        Review review = reviewRepository.findByIdWithRelations(reviewId)
                .orElseThrow(() -> new CustomException("리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        return convertToResponseDTO(review, currentUserUUID);
    }

    @Override
    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateDTO updateDTO) {
        UUID currentUserUUID = getCurrentUserUUID();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException("리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 리뷰 작성자 확인
        if (!review.getUserApply().getPetUser().getUserId().equals(currentUserUUID)) {
            throw new CustomException("리뷰를 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        // 리뷰 업데이트
        if (updateDTO.rating() != null) {
            review.setRating(updateDTO.rating());
        }
        if (updateDTO.title() != null) {
            review.setTitle(updateDTO.title());
        }
        if (updateDTO.comment() != null) {
            review.setComment(updateDTO.comment());
        }
        if (updateDTO.reviewImageUrl() != null) {
            review.setReviewImageUrl(updateDTO.reviewImageUrl());
        }

        Review updatedReview = reviewRepository.save(review);
        return convertToResponseDTO(updatedReview, currentUserUUID);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        UUID currentUserUUID = getCurrentUserUUID();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException("리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 리뷰 작성자 확인
        if (!review.getUserApply().getPetUser().getUserId().equals(currentUserUUID)) {
            throw new CustomException("리뷰를 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        reviewRepository.delete(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByTrainerId(UUID trainerId) {
        UUID currentUserUUID = getCurrentUserUUID();
        List<Review> reviews = reviewRepository.findByUserApply_Trainer_TrainerId(trainerId);

        return reviews.stream()
                .map(review -> convertToResponseDTO(review, currentUserUUID))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getMyReviews() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        UUID userId = null;
        if (principal instanceof CustomOAuth2User oAuth2User) {
            userId = oAuth2User.getUserId();
            System.out.println("사용자 ID: " + userId);
        }

        UUID currentUserUUID = getCurrentUserUUID();
        System.out.println("currentUserUUID = " + userId);
        List<Review> reviews = reviewRepository.findByUserApply_PetUser_UserId(userId);

        return reviews.stream()
                .map(review -> convertToResponseDTO(review, currentUserUUID))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResponseEntity<?> toggleLikeForReview(Long reviewId) {
        PetUser currentUser = getCurrentUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        // 이미 좋아요가 있는지 확인
        Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewAndUser(review, currentUser);

        if (existingLike.isPresent()) {
            // 좋아요가 있으면 삭제
            reviewLikeRepository.delete(existingLike.get());
            return ResponseEntity.ok().body(Map.of(
                    "status", "removed",
                    "message", "좋아요가 취소되었습니다."
            ));
        } else {
            // 좋아요가 없으면 추가
            ReviewLike reviewLike = new ReviewLike();
            reviewLike.setReview(review);
            reviewLike.setUser(currentUser);

            ReviewLike savedLike = reviewLikeRepository.save(reviewLike);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String createdAt = java.time.LocalDateTime.now().format(formatter);

            ReviewLikeResponseDTO response = new ReviewLikeResponseDTO(
                    savedLike.getLikeId(),
                    review.getReviewId(),
                    currentUser.getUserId(),
                    createdAt
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "added",
                    "data", response,
                    "message", "좋아요가 추가되었습니다."
            ));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewLikeCountDTO getReviewLikesCount(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        Integer likeCount = reviewLikeRepository.countByReview(review);

        return new ReviewLikeCountDTO(reviewId, likeCount);
    }

    private ReviewResponseDTO convertToResponseDTO(Review review, UUID currentUserUUID) {
        Integer likeCount = reviewLikeRepository.countByReview(review);
        Boolean hasLiked = reviewLikeRepository.existsByReviewAndUser_UserId(review, currentUserUUID);
        return convertToResponseDTO(review, currentUserUUID, likeCount, hasLiked);
    }

    // ReviewEntity를 ReviewResponseDTO로 변환
    private ReviewResponseDTO convertToResponseDTO(
            Review review,
            UUID currentUserUUID,
            Integer likeCount,
            Boolean hasLiked) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String createdAt = review.getCreatedAt() != null ?
                review.getCreatedAt().format(formatter) : null;

        String updatedAt = review.getUpdatedAt() != null ?
                review.getUpdatedAt().format(formatter) : null;

        UserApply userApply = review.getUserApply();

        return new ReviewResponseDTO(
                review.getReviewId(),
                userApply.getApplyId(),
                userApply.getPetUser().getUserId(),
                userApply.getPetUser().getName(),
                userApply.getTrainer().getTrainerId(),
                userApply.getTrainer().getUser().getName(),
                review.getRating(),
                review.getTitle(),
                review.getComment(),
                review.getReviewImageUrl(),
                likeCount,
                hasLiked,
                createdAt,
                updatedAt
        );
    }

    private UUID getCurrentUserUUID() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal() instanceof CustomOAuth2User userDetails) {
            return userDetails.getUserId();
        }

        throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED);
//        throw ExceptionUtils.of(ErrorCode.UNAUTHORIZED);
    }

    private PetUser getCurrentUser() {
        UUID currentUserUUID = getCurrentUserUUID();
        return petUserRepository.findById(currentUserUUID)
                .orElseThrow(() -> new CustomException("사용자가 없습니다", HttpStatus.NOT_FOUND));
    }


}
