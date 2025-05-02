package org.lucky0111.pettalk.repository.review;

import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.domain.entity.review.Review;
import org.lucky0111.pettalk.domain.entity.review.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    // 기존 메서드는 유지
    boolean existsByReviewAndUser(Review review, PetUser user);
    boolean existsByReviewAndUser_UserId(Review review, UUID userId);
    Optional<ReviewLike> findByReviewAndUser(Review review, PetUser user);
    Integer countByReview(Review review);
    void deleteByReview(Review review);

    // 수정된 집계 쿼리 (fetch join 제거)
    @Query("SELECT r.reviewId as reviewId, COUNT(rl.likeId) as likeCount " +
            "FROM Review r " +
            "LEFT JOIN ReviewLike rl ON rl.review = r " +
            "WHERE r.reviewId IN :reviewIds " +
            "GROUP BY r.reviewId")
    List<ReviewLikeCountProjection> countLikesByReviewIds(@Param("reviewIds") List<Long> reviewIds);

    // 수정된 사용자 좋아요 여부 쿼리 (fetch join 제거)
    @Query("SELECT r.reviewId as reviewId, " +
            "CASE WHEN COUNT(rl.likeId) > 0 THEN true ELSE false END as hasLiked " +
            "FROM Review r " +
            "LEFT JOIN ReviewLike rl ON rl.review = r AND rl.user.userId = :userId " +
            "WHERE r.reviewId IN :reviewIds " +
            "GROUP BY r.reviewId")
    List<ReviewLikeStatusProjection> checkUserLikeStatus(
            @Param("reviewIds") List<Long> reviewIds,
            @Param("userId") UUID userId);

    // 프로젝션 인터페이스 추가
    interface ReviewLikeCountProjection {
        Long getReviewId();
        Integer getLikeCount();
    }

    interface ReviewLikeStatusProjection {
        Long getReviewId();
        Boolean getHasLiked();
    }
}