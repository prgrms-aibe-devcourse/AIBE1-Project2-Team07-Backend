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
}
