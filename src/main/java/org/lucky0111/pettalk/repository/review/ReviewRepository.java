package org.lucky0111.pettalk.repository.review;

import org.lucky0111.pettalk.domain.entity.match.UserApply;
import org.lucky0111.pettalk.domain.entity.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Query("SELECT ua.trainer.trainerId as trainerId, AVG(r.rating) as avgRating " +
            "FROM Review r JOIN r.userApply ua " +
            "WHERE ua.trainer.trainerId IN :trainerIds " +
            "GROUP BY ua.trainer.trainerId")
    List<TrainerRatingProjection> findAverageRatingsByTrainerIdsRaw(Collection<UUID> trainerIds);

    default Map<UUID, Double> findAverageRatingsByTrainerIds(Collection<UUID> trainerIds) {
        return findAverageRatingsByTrainerIdsRaw(trainerIds).stream()
                .collect(Collectors.toMap(
                        TrainerRatingProjection::getTrainerId,
                        projection -> projection.getAvgRating() != null ? projection.getAvgRating() : 0.0
                ));
    }

    @Query("SELECT ua.trainer.trainerId as trainerId, COUNT(r) as reviewCount " +
            "FROM Review r JOIN r.userApply ua " +
            "WHERE ua.trainer.trainerId IN :trainerIds " +
            "GROUP BY ua.trainer.trainerId")
    List<TrainerReviewCountProjection> findReviewCountsByTrainerIdsRaw(Collection<UUID> trainerIds);

    default Map<UUID, Long> countReviewsByTrainerIds(Collection<UUID> trainerIds) {
        return findReviewCountsByTrainerIdsRaw(trainerIds).stream()
                .collect(Collectors.toMap(
                        TrainerReviewCountProjection::getTrainerId,
                        TrainerReviewCountProjection::getReviewCount
                ));
    }

    Review findByUserApply_ApplyId(Long applyId);

    interface TrainerRatingProjection {
        UUID getTrainerId();
        Double getAvgRating();
    }

    interface TrainerReviewCountProjection {
        UUID getTrainerId();
        Long getReviewCount();
    }
}
