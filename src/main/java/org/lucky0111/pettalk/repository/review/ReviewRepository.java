package org.lucky0111.pettalk.repository.review;

import org.lucky0111.pettalk.domain.entity.match.UserApply;
import org.lucky0111.pettalk.domain.entity.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUserApply(UserApply userApply);
    List<Review> findByUserApply_Trainer_TrainerId(UUID trainerId);
    List<Review> findByUserApply_PetUser_UserId(UUID userId);

    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.userApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user")
    List<Review> findAllWithRelations();

    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.userApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE t.trainerId = :trainerId")
    List<Review> findByTrainerIdWithRelations(UUID trainerId);

    // 사용자별 리뷰 조회 최적화
    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.userApply ua " +
            "JOIN FETCH ua.petUser pu " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE pu.userId = :userId")
    List<Review> findByUserIdWithRelations(UUID userId);

    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.userApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE r.reviewId = :reviewId")
    Optional<Review> findByIdWithRelations(@Param("reviewId") Long reviewId);

    // 특정 Trainer의 평균 평점을 조회하는 쿼리 (관계 필드 조인 사용)
    // Review(r) 엔티티의 userApply 관계 필드를 타고 UserApply(ua) 엔티티 조인
    // UserApply(ua) 엔티티의 trainer 관계 필드를 타고 Trainer(t) 엔티티 조인 ( Trainer 엔티티의 PK 필드는 trainerId 임)
    @Query("SELECT AVG(r.rating) FROM Review r JOIN r.userApply ua JOIN ua.trainer t WHERE t.trainerId = :trainerId")
    Double findAverageRatingByTrainerId(@Param("trainerId") UUID trainerId);

    // 특정 Trainer의 후기(Review) 개수를 조회하는 쿼리 (관계 필드 조인 사용)
    // 위와 동일하게 관계 필드를 타고 조인
    @Query("SELECT COUNT(r) FROM Review r JOIN r.userApply ua JOIN ua.trainer t WHERE t.trainerId = :trainerId")
    Long countByReviewedTrainerId(@Param("trainerId") UUID trainerId);

}
