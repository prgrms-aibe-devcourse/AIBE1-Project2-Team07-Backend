package org.lucky0111.pettalk.service.review;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.common.Status;
import org.lucky0111.pettalk.domain.dto.review.*;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.domain.entity.match.UserApply;
import org.lucky0111.pettalk.domain.entity.review.Review;
import org.lucky0111.pettalk.domain.entity.review.ReviewLike;
import org.lucky0111.pettalk.repository.match.UserApplyRepository;
import org.lucky0111.pettalk.repository.review.ReviewLikeRepository;
import org.lucky0111.pettalk.repository.review.ReviewRepository;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.lucky0111.pettalk.util.auth.JWTUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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
    public ReviewResponseDTO createReview(ReviewRequestDTO requestDTO,HttpServletRequest request) throws AccessDeniedException {
        UUID currentUserUUID = getCurrentUserUUID(request);
        PetUser currentUser = getCurrentUser(request);

        // 신청서 조회
        UserApply userApply = userApplyRepository.findById(requestDTO.applyId())
                .orElseThrow(() -> new EntityNotFoundException("해당 신청서를 찾을 수 없습니다."));

        // 신청서 작성자 확인
        if (!userApply.getPetUser().getUserId().equals(currentUserUUID)) {
            throw new AccessDeniedException("리뷰를 작성할 권한이 없습니다.");
        }

        if (userApply.getStatus() != Status.APPROVED) {
            throw new IllegalStateException("승인된 신청에 대해서만 리뷰를 작성할 수 있습니다.");
        }

        // 이미 리뷰가 존재하는지 확인
        if (reviewRepository.existsByUserApply(userApply)) {
            throw new IllegalStateException("이미 리뷰가 존재합니다.");
        }

        // 리뷰 생성
        Review review = new Review();
        review.setUserApply(userApply);
        review.setRating(requestDTO.rating());
        review.setTitle(requestDTO.title());
        review.setComment(requestDTO.comment());
        review.setReviewImageUrl(requestDTO.reviewImageUrl());

        Review savedReview = reviewRepository.save(review);

        return convertToResponseDTO(savedReview, currentUserUUID);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getAllReviews() {
        UUID currentUserUUID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");

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
    public ReviewResponseDTO getReviewById(Long reviewId, HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);
        Review review = reviewRepository.findByIdWithRelations(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        return convertToResponseDTO(review, currentUserUUID);
    }

    @Override
    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateDTO updateDTO, HttpServletRequest request) throws AccessDeniedException {
        UUID currentUserUUID = getCurrentUserUUID(request);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        // 리뷰 작성자 확인
        if (!review.getUserApply().getPetUser().getUserId().equals(currentUserUUID)) {
            throw new AccessDeniedException("리뷰를 수정할 권한이 없습니다.");
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
    public void deleteReview(Long reviewId, HttpServletRequest request) throws AccessDeniedException {
        UUID currentUserUUID = getCurrentUserUUID(request);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        // 리뷰 작성자 확인
        if (!review.getUserApply().getPetUser().getUserId().equals(currentUserUUID)) {
            throw new AccessDeniedException("리뷰를 삭제할 권한이 없습니다.");
        }

        reviewRepository.delete(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByTrainerId(UUID trainerId, HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);
        List<Review> reviews = reviewRepository.findByUserApply_Trainer_TrainerId(trainerId);

        return reviews.stream()
                .map(review -> convertToResponseDTO(review, currentUserUUID))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getMyReviews(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        System.out.println("username = " + username);

        UUID currentUserUUID = getCurrentUserUUID(request);
        System.out.println("currentUserUUID = " + currentUserUUID);
        List<Review> reviews = reviewRepository.findByUserApply_PetUser_UserId(currentUserUUID);

        return reviews.stream()
                .map(review -> convertToResponseDTO(review, currentUserUUID))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewLikeResponseDTO addLikeToReview(Long reviewId, HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);
        PetUser currentUser = getCurrentUser(request);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        // 이미 좋아요가 있는지 확인
        if (reviewLikeRepository.existsByReviewAndUser(review, currentUser)) {
            throw new IllegalStateException("이미 좋아요를 누르셨습니다.");
        }

        // 좋아요 생성
        ReviewLike reviewLike = new ReviewLike();
        reviewLike.setReview(review);
        reviewLike.setUser(currentUser);

        ReviewLike savedLike = reviewLikeRepository.save(reviewLike);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createdAt = java.time.LocalDateTime.now().format(formatter);

        return new ReviewLikeResponseDTO(
                savedLike.getLikeId(),
                review.getReviewId(),
                currentUserUUID,
                createdAt
        );
    }

    @Override
    @Transactional
    public void removeLikeFromReview(Long reviewId, HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);
        PetUser currentUser = getCurrentUser(request);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        // 좋아요 확인 및 삭제
        ReviewLike reviewLike = reviewLikeRepository.findByReviewAndUser(review, currentUser)
                .orElseThrow(() -> new IllegalStateException("좋아요를 찾을 수 없습니다."));

        reviewLikeRepository.delete(reviewLike);
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

    private String extractJwtToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        System.out.println("bearerToken = " + bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {

            return bearerToken.substring(7);
        }
        return null;
    }

    // 현재 인증된 사용자의 UUID 가져오기
    private UUID getCurrentUserUUID(HttpServletRequest request) {
        String token = extractJwtToken(request);
        if (token == null) {
            throw new RuntimeException("인증 토큰을 찾을 수 없습니다.");
        }
        return jwtUtil.getUserId(token);
    }

    // 현재 사용자 엔티티 가져오기
    private PetUser getCurrentUser(HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);
        return petUserRepository.findById(currentUserUUID)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }


}
