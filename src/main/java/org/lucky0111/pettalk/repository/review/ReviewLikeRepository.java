package org.lucky0111.pettalk.repository.review;

import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.domain.entity.review.Review;
import org.lucky0111.pettalk.domain.entity.review.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    boolean existsByReviewAndUser(Review review, PetUser user);
    boolean existsByReviewAndUser_UserId(Review review, UUID userId);
    Optional<ReviewLike> findByReviewAndUser(Review review, PetUser user);
    Integer countByReview(Review review);
    void deleteByReview(Review review);

    @Query("SELECT r.reviewId, COUNT(rl) FROM ReviewLike rl " +
            "RIGHT JOIN rl.review r " +
            "WHERE r.reviewId IN :reviewIds " +
            "GROUP BY r.reviewId")
    List<Object[]> countByReviewIdsRaw(List<Long> reviewIds);

    // 결과를 Map으로 변환하는 default 메소드
    default Map<Long, Integer> countByReviewIds(List<Long> reviewIds) {
        return countByReviewIdsRaw(reviewIds).stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> ((Long) arr[1]).intValue()
                ));
    }

    // 사용자가 여러 리뷰에 좋아요했는지 한 번에 조회
    @Query("SELECT r.reviewId, CASE WHEN COUNT(rl) > 0 THEN true ELSE false END " +
            "FROM Review r " +
            "LEFT JOIN ReviewLike rl ON r.reviewId = rl.review.reviewId AND rl.user.userId = :userId " +
            "WHERE r.reviewId IN :reviewIds " +
            "GROUP BY r.reviewId")
    List<Object[]> existsByReviewIdsAndUserIdRaw(List<Long> reviewIds, UUID userId);

    // 결과를 Map으로 변환하는 default 메소드
    default Map<Long, Boolean> existsByReviewIdsAndUserId(List<Long> reviewIds, UUID userId) {
        return existsByReviewIdsAndUserIdRaw(reviewIds, userId).stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Boolean) arr[1]
                ));
    }
}