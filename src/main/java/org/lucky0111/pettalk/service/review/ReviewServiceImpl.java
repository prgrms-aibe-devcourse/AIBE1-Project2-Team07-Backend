package org.lucky0111.pettalk.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.common.ApplyStatus;
import org.lucky0111.pettalk.domain.common.ErrorCode;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.dto.review.*;
import org.lucky0111.pettalk.domain.entity.trainer.Trainer;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.domain.entity.match.UserApply;
import org.lucky0111.pettalk.domain.entity.review.Review;
import org.lucky0111.pettalk.domain.entity.review.ReviewLike;
import org.lucky0111.pettalk.exception.CustomException;
import org.lucky0111.pettalk.repository.match.UserApplyRepository;
import org.lucky0111.pettalk.repository.review.ReviewLikeRepository;
import org.lucky0111.pettalk.repository.review.ReviewRepository;
import org.lucky0111.pettalk.repository.trainer.TrainerRepository;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserApplyRepository userApplyRepository;
    private final PetUserRepository petUserRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final TrainerRepository trainerRepository;

    @Override
    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO requestDTO) {
        PetUser currentUser = getCurrentUser();
        UserApply userApply = findUserApplyById(requestDTO.applyId());

        validateUserPermission(userApply, currentUser.getUserId());
        validateApplyStatus(userApply);
        validateNoExistingReview(userApply);

        userApply.setHasReviewed(true);
        userApplyRepository.save(userApply);

        Review review = buildReviewFromRequest(requestDTO, userApply);
        Review savedReview = reviewRepository.save(review);

        return convertToResponseDTO(savedReview, currentUser.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getAllReviews() {
        UUID currentUserUUID = getCurrentUserUUID();
        List<Review> reviews = reviewRepository.findAllWithRelations();

        Map<Long, Integer> likeCounts = getLikeCountsMap(reviews);
        Map<Long, Boolean> userLikedMap = getUserLikedStatusMap(reviews, currentUserUUID);

        return convertToResponseDTOList(reviews, likeCounts, userLikedMap);
    }

    @Override
    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateDTO updateDTO) {
        UUID currentUserUUID = getCurrentUserUUID();
        Review review = findReviewById(reviewId);

        validateReviewOwnership(review, currentUserUUID);
        updateReviewFields(review, updateDTO);

        Review updatedReview = reviewRepository.save(review);
        return convertToResponseDTO(updatedReview, currentUserUUID);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        UUID currentUserUUID = getCurrentUserUUID();
        Review review = findReviewById(reviewId);

        UserApply userApply = review.getUserApply();
        userApply.setHasReviewed(false);
        userApplyRepository.save(userApply);

        validateReviewOwnership(review, currentUserUUID);
        reviewRepository.delete(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByTrainerNickname(String trainerNickname) {
        UUID currentUserUUID = getCurrentUserUUID();
        Trainer trainer = trainerRepository.findByUser_Nickname(trainerNickname)
                .orElseThrow(() -> new CustomException(ErrorCode.TRAINER_NOT_FOUND));

        List<Review> reviews = reviewRepository.findByUserApply_Trainer_TrainerId(trainer.getTrainerId());

        return reviews.stream()
                .map(review -> convertToResponseDTO(review, currentUserUUID))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getMyReviews() {
        UUID currentUserUUID = getCurrentUserUUID();

        List<Review> reviews = reviewRepository.findByUserApply_PetUser_UserId(currentUserUUID);

        Map<Long, Integer> likeCounts = getLikeCountsMap(reviews);
        Map<Long, Boolean> userLikedMap = getUserLikedStatusMap(reviews, currentUserUUID);

        return convertToResponseDTOList(reviews, likeCounts, userLikedMap);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getMyTrainerReviews(){
        UUID currentUserUUID = getCurrentUserUUID();

        List<Review> reviews = reviewRepository.findByUserApply_Trainer_TrainerId(currentUserUUID);

        Map<Long, Integer> likeCounts = getLikeCountsMap(reviews);
        Map<Long, Boolean> userLikedMap = getUserLikedStatusMap(reviews, currentUserUUID);

        return convertToResponseDTOList(reviews, likeCounts, userLikedMap);
    }

    @Override
    @Transactional
    public ResponseEntity<?> toggleLikeForReview(Long reviewId) {
        PetUser currentUser = getCurrentUser();
        Review review = findReviewById(reviewId);

        Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewAndUser(review, currentUser);

        if (existingLike.isPresent()) {
            return handleRemoveLike(existingLike.get());
        } else {
            return handleAddLike(review, currentUser);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getTopLikedReviews(int limit) {
        UUID currentUserUUID = getCurrentUserUUID();

        List<Long> topReviewIds = findTopReviewIdsByLikeCount(limit);
        if (topReviewIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Review> reviews = fetchReviewsWithRelations(topReviewIds);

        sortReviewsByLikeCount(reviews, topReviewIds);

        Map<Long, Integer> likeCounts = getLikeCountsMap(reviews);
        Map<Long, Boolean> userLikedMap = getUserLikedStatusMap(reviews, currentUserUUID);

        return convertToResponseDTOList(reviews, likeCounts, userLikedMap);
    }

    private Review buildReviewFromRequest(ReviewRequestDTO requestDTO, UserApply userApply) {
        Review review = new Review();
        review.setUserApply(userApply);
        review.setRating(requestDTO.rating());
        review.setTitle(requestDTO.title());
        review.setComment(requestDTO.comment());
        review.setReviewImageUrl(requestDTO.reviewImageUrl());
        return review;
    }

    private void validateUserPermission(UserApply userApply, UUID userId) {
        if (!userApply.getPetUser().getUserId().equals(userId)) {
            throw new CustomException("리뷰를 작성할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
    }

    private void validateApplyStatus(UserApply userApply) {
        if (userApply.getApplyStatus() != ApplyStatus.APPROVED) {
            throw new CustomException("승인된 신청에 대해서만 리뷰를 작성할 수 있습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateNoExistingReview(UserApply userApply) {
        if (reviewRepository.existsByUserApply(userApply)) {
            throw new CustomException("이미 리뷰가 존재합니다.", HttpStatus.CONFLICT);
        }
    }

    private UserApply findUserApplyById(Long applyId) {
        return userApplyRepository.findById(applyId)
                .orElseThrow(() -> new CustomException("해당 신청서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private Review findReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException("리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private void validateReviewOwnership(Review review, UUID userId) {
        if (!review.getUserApply().getPetUser().getUserId().equals(userId)) {
            throw new CustomException("리뷰를 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
    }

    private void updateReviewFields(Review review, ReviewUpdateDTO updateDTO) {
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
    }

    private Map<Long, Integer> getLikeCountsMap(List<Review> reviews) {
        return reviewLikeRepository.countLikesByReviewIds(
                        reviews.stream().map(Review::getReviewId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(
                        ReviewLikeRepository.ReviewLikeCountProjection::getReviewId,
                        ReviewLikeRepository.ReviewLikeCountProjection::getLikeCount
                ));
    }

    private Map<Long, Boolean> getUserLikedStatusMap(List<Review> reviews, UUID userUUID) {
        return reviewLikeRepository.checkUserLikeStatus(
                        reviews.stream().map(Review::getReviewId).collect(Collectors.toList()),
                        userUUID)
                .stream()
                .collect(Collectors.toMap(
                        ReviewLikeRepository.ReviewLikeStatusProjection::getReviewId,
                        ReviewLikeRepository.ReviewLikeStatusProjection::getHasLiked
                ));
    }

    private List<ReviewResponseDTO> convertToResponseDTOList(
            List<Review> reviews,
            Map<Long, Integer> likeCounts,
            Map<Long, Boolean> userLikedMap) {
        return reviews.stream()
                .map(review -> convertToResponseDTO(
                        review,
                        likeCounts.getOrDefault(review.getReviewId(), 0),
                        userLikedMap.getOrDefault(review.getReviewId(), false)))
                .collect(Collectors.toList());
    }

    private ResponseEntity<?> handleRemoveLike(ReviewLike reviewLike) {
        reviewLikeRepository.delete(reviewLike);
        return ResponseEntity.ok().body(Map.of(
                "status", "removed",
                "message", "좋아요가 취소되었습니다."
        ));
    }

    private ResponseEntity<?> handleAddLike(Review review, PetUser currentUser) {
        ReviewLike reviewLike = createReviewLike(review, currentUser);
        ReviewLike savedLike = reviewLikeRepository.save(reviewLike);

        String createdAt = formatCurrentDateTime();
        ReviewLikeResponseDTO response = createReviewLikeResponse(savedLike, review, currentUser, createdAt);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", "added",
                "data", response,
                "message", "좋아요가 추가되었습니다."
        ));
    }

    private ReviewLike createReviewLike(Review review, PetUser user) {
        ReviewLike reviewLike = new ReviewLike();
        reviewLike.setReview(review);
        reviewLike.setUser(user);
        return reviewLike;
    }

    private ReviewLikeResponseDTO createReviewLikeResponse(
            ReviewLike savedLike,
            Review review,
            PetUser currentUser,
            String createdAt) {
        return new ReviewLikeResponseDTO(
                savedLike.getLikeId(),
                review.getReviewId(),
                currentUser.getUserId(),
                createdAt
        );
    }

    private List<Long> findTopReviewIdsByLikeCount(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return reviewLikeRepository.findTopLikedReviewIds(pageable).stream()
                .map(ReviewLikeRepository.ReviewLikeCountProjection::getReviewId)
                .collect(Collectors.toList());
    }

    // 리뷰 및 관련 데이터 조회
    private List<Review> fetchReviewsWithRelations(List<Long> reviewIds) {
        return reviewRepository.findAllByIdWithRelations(reviewIds);
    }

    // 좋아요 순서대로 정렬
    private void sortReviewsByLikeCount(List<Review> reviews, List<Long> orderedReviewIds) {
        Map<Long, Integer> reviewIdToIndex = new HashMap<>();
        for (int i = 0; i < orderedReviewIds.size(); i++) {
            reviewIdToIndex.put(orderedReviewIds.get(i), i);
        }

        reviews.sort(Comparator.comparing(review ->
                reviewIdToIndex.getOrDefault(review.getReviewId(), Integer.MAX_VALUE)));
    }

    private String formatCurrentDateTime() {
        return LocalDateTime.now().format(DATE_FORMATTER);
    }

    private ReviewResponseDTO convertToResponseDTO(Review review, UUID currentUserUUID) {
        Integer likeCount = reviewLikeRepository.countByReview(review);
        Boolean hasLiked = reviewLikeRepository.existsByReviewAndUser_UserId(review, currentUserUUID);
        return convertToResponseDTO(review, likeCount, hasLiked);
    }

    private ReviewResponseDTO convertToResponseDTO(
            Review review,
            Integer likeCount,
            Boolean hasLiked) {

        String createdAt = formatDateTime(review.getCreatedAt());
        String updatedAt = formatDateTime(review.getUpdatedAt());
        UserApply userApply = review.getUserApply();

        return new ReviewResponseDTO(
                review.getReviewId(),
                userApply.getApplyId(),
                userApply.getPetUser().getName(),
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

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : null;
    }

    private UUID getCurrentUserUUID() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal() instanceof CustomOAuth2User userDetails) {
            return userDetails.getUserId();
        }

        throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED);
    }

    private PetUser getCurrentUser() {
        UUID currentUserUUID = getCurrentUserUUID();
        return petUserRepository.findById(currentUserUUID)
                .orElseThrow(() -> new CustomException("사용자가 없습니다", HttpStatus.NOT_FOUND));
    }
}