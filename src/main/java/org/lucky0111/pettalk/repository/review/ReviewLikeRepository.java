package org.lucky0111.pettalk.repository.review;

import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.domain.entity.review.Review;
import org.lucky0111.pettalk.domain.entity.review.ReviewLike;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    // 기존 메서드는 유지
    boolean existsByReviewAndUser_UserId(Review review, UUID userId);
    Optional<ReviewLike> findByReviewAndUser(Review review, PetUser user);
    Integer countByReview(Review review);

    @Query("SELECT r.reviewId as reviewId, COUNT(rl.likeId) as likeCount " +
            "FROM Review r " +
            "LEFT JOIN ReviewLike rl ON rl.review = r " +
            "WHERE r.reviewId IN :reviewIds " +
            "GROUP BY r.reviewId")
    List<ReviewLikeCountProjection> countLikesByReviewIds(@Param("reviewIds") List<Long> reviewIds);

    @Query("SELECT r.reviewId as reviewId, " +
            "CASE WHEN COUNT(rl.likeId) > 0 THEN true ELSE false END as hasLiked " +
            "FROM Review r " +
            "LEFT JOIN ReviewLike rl ON rl.review = r AND rl.user.userId = :userId " +
            "WHERE r.reviewId IN :reviewIds " +
            "GROUP BY r.reviewId")
    List<ReviewLikeStatusProjection> checkUserLikeStatus(
            @Param("reviewIds") List<Long> reviewIds,
            @Param("userId") UUID userId);

    @Query("SELECT rl.review.reviewId as reviewId, COUNT(rl.likeId) as likeCount " +
            "FROM ReviewLike rl " +
            "GROUP BY rl.review.reviewId " +
            "ORDER BY COUNT(rl.likeId) DESC")
    List<ReviewLikeCountProjection> findTopLikedReviewIds(Pageable pageable);

    interface ReviewLikeCountProjection {
        Long getReviewId();
        Integer getLikeCount();
    }

    interface ReviewLikeStatusProjection {
        Long getReviewId();
        Boolean getHasLiked();
    }
}