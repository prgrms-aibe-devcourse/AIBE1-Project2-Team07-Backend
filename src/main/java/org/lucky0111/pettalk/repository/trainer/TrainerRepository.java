package org.lucky0111.pettalk.repository.trainer;

import org.lucky0111.pettalk.domain.entity.trainer.Trainer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainerRepository extends JpaRepository<Trainer, UUID> {
    Optional<Trainer> findByUser_Nickname(String nickname);

    @Query("SELECT DISTINCT t FROM Trainer t " +
            "JOIN FETCH t.user u " +
            "LEFT JOIN FETCH t.photos " +
            "LEFT JOIN FETCH t.serviceFees")
    List<Trainer> findAllWithPhotosAndServiceFees(Pageable pageable);

    @Query("SELECT DISTINCT t FROM Trainer t " +
            "JOIN FETCH t.user u " +
            "LEFT JOIN FETCH t.photos p " +
            "LEFT JOIN FETCH t.serviceFees sf " +
            "LEFT JOIN FETCH t.trainerTagRelations ttr " +
            "WHERE t.trainerId = :trainerId")
    Optional<Trainer> findByIdWithProfileCollections(@Param("trainerId") UUID trainerId);

    @Query("SELECT DISTINCT t FROM Trainer t " +
            "JOIN FETCH t.user u " +
            "LEFT JOIN FETCH t.photos " +
            "LEFT JOIN FETCH t.serviceFees " +
            "ORDER BY t.createdAt DESC")
    List<Trainer> findAllWithPhotosAndServiceFeesByLatest(Pageable pageable);

    @Query(value = "SELECT DISTINCT t.* FROM trainers t " +
            "JOIN pet_users u ON t.trainer_id = u.user_id " +
            "LEFT JOIN (" +
            "   SELECT ua.trainer_id, COUNT(r.review_id) as review_count " +
            "   FROM reviews r " +
            "   JOIN user_applies ua ON r.apply_id = ua.apply_id " +
            "   GROUP BY ua.trainer_id" +
            ") review_counts ON t.trainer_id = review_counts.trainer_id " +
            "ORDER BY COALESCE(review_counts.review_count, 0) DESC",
            nativeQuery = true)
    List<Trainer> findAllWithPhotosAndServiceFeesByReviewCount(Pageable pageable);

    @Query(value = "SELECT DISTINCT t.* FROM trainers t " +
            "JOIN pet_users u ON t.trainer_id = u.user_id " +
            "LEFT JOIN (" +
            "   SELECT ua.trainer_id, AVG(r.rating) as avg_rating " +
            "   FROM reviews r " +
            "   JOIN user_applies ua ON r.apply_id = ua.apply_id " +
            "   GROUP BY ua.trainer_id" +
            ") rating_avgs ON t.trainer_id = rating_avgs.trainer_id " +
            "ORDER BY COALESCE(rating_avgs.avg_rating, 0) DESC",
            nativeQuery = true)
    List<Trainer> findAllWithPhotosAndServiceFeesByRating(Pageable pageable);

    @Query("SELECT COUNT(t) FROM Trainer t")
    long countTrainers();
}
