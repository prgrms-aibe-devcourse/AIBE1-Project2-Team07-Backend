package org.lucky0111.pettalk.repository.review;

import org.lucky0111.pettalk.domain.entity.match.UserApply;
import org.lucky0111.pettalk.domain.entity.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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
            "WHERE r.reviewId IN :reviewIds")
    List<Review> findAllByIdWithRelations(@Param("reviewIds") List<Long> reviewIds);

    @Query("SELECT AVG(r.rating) FROM Review r JOIN r.userApply ua JOIN ua.trainer t WHERE t.trainerId = :trainerId")
    Double findAverageRatingByTrainerId(@Param("trainerId") UUID trainerId);

    @Query("SELECT COUNT(r) FROM Review r JOIN r.userApply ua JOIN ua.trainer t WHERE t.trainerId = :trainerId")
    Long countByReviewedTrainerId(@Param("trainerId") UUID trainerId);

}
