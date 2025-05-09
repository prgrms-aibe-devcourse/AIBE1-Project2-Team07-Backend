package org.lucky0111.pettalk.repository.trainer;

import org.lucky0111.pettalk.domain.entity.trainer.Trainer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    @EntityGraph(attributePaths = {"user", "photos", "serviceFees"})
    Page<Trainer> findAll(Specification<Trainer> spec, Pageable pageable);

    @Query(value = "SELECT t.trainer_id, COUNT(r.review_id) as review_count " +
            "FROM trainers t " +
            "LEFT JOIN user_applies ua ON t.trainer_id = ua.trainer_id " +
            "LEFT JOIN reviews r ON ua.apply_id = r.apply_id " +
            "WHERE t.trainer_id IN :trainerIds " +
            "GROUP BY t.trainer_id",
            nativeQuery = true)
    List<Object[]> countReviewsByTrainerIdsForSort(@Param("trainerIds") List<UUID> trainerIds);

    @Query(value = "SELECT t.trainer_id as trainer_id, COALESCE(AVG(r.rating), 0) as avg_rating " +
            "FROM trainers t " +
            "LEFT JOIN user_applies ua ON t.trainer_id = ua.trainer_id " +
            "LEFT JOIN reviews r ON ua.apply_id = r.apply_id " +
            "WHERE t.trainer_id IN :trainerIds " +
            "GROUP BY t.trainer_id",
            nativeQuery = true)
    List<Object[]> findAverageRatingsByTrainerIdsForSort(@Param("trainerIds") List<UUID> trainerIds);

    @Query(value = "SELECT DISTINCT t.* FROM trainers t " +
            "JOIN pet_users u ON t.trainer_id = u.user_id " +
            "LEFT JOIN (" +
            "   SELECT ua.trainer_id, COUNT(r.review_id) as review_count " +
            "   FROM reviews r " +
            "   JOIN user_applies ua ON r.apply_id = ua.apply_id " +
            "   GROUP BY ua.trainer_id" +
            ") review_counts ON t.trainer_id = review_counts.trainer_id " +
            "WHERE (:keyword IS NULL OR " +
            "      (:searchType = 'ALL' AND (LOWER(t.title) LIKE CONCAT('%', LOWER(:keyword), '%') OR " +
            "                               LOWER(t.introduction) LIKE CONCAT('%', LOWER(:keyword), '%') OR " +
            "                               LOWER(t.visiting_areas) LIKE CONCAT('%', LOWER(:keyword), '%'))) OR " +
            "      (:searchType = 'TITLE' AND LOWER(t.title) LIKE CONCAT('%', LOWER(:keyword), '%')) OR " +
            "      (:searchType = 'CONTENT' AND LOWER(t.introduction) LIKE CONCAT('%', LOWER(:keyword), '%')) OR " +
            "      (:searchType = 'LOCATION' AND LOWER(t.visiting_areas) LIKE CONCAT('%', LOWER(:keyword), '%'))) " +
            "ORDER BY " +
            "CASE WHEN :sortType = 'REVIEWS' THEN COALESCE(review_counts.review_count, 0) END DESC, " +
            "CASE WHEN :sortType = 'RATING' THEN " +
            "  (SELECT COALESCE(AVG(r.rating), 0) FROM reviews r " +
            "   JOIN user_applies ua ON r.apply_id = ua.apply_id " +
            "   WHERE ua.trainer_id = t.trainer_id) " +
            "END DESC, " +
            "CASE WHEN :sortType = 'LATEST' OR :sortType NOT IN ('REVIEWS', 'RATING') THEN t.created_at END DESC " +
            "LIMIT :pageSize OFFSET :offset",
            nativeQuery = true)
    List<Trainer> searchTrainersWithSort(
            @Param("keyword") String keyword,
            @Param("searchType") String searchType,
            @Param("sortType") String sortType,
            @Param("pageSize") int pageSize,
            @Param("offset") int offset);

    @Query(value = "SELECT COUNT(DISTINCT t.trainer_id) FROM trainers t " +
            "JOIN pet_users u ON t.trainer_id = u.user_id " +
            "WHERE (:keyword IS NULL OR " +
            "      (:searchType = 'ALL' AND (LOWER(t.title) LIKE CONCAT('%', LOWER(:keyword), '%') OR " +
            "                               LOWER(t.introduction) LIKE CONCAT('%', LOWER(:keyword), '%') OR " +
            "                               LOWER(t.visiting_areas) LIKE CONCAT('%', LOWER(:keyword), '%'))) OR " +
            "      (:searchType = 'TITLE' AND LOWER(t.title) LIKE CONCAT('%', LOWER(:keyword), '%')) OR " +
            "      (:searchType = 'CONTENT' AND LOWER(t.introduction) LIKE CONCAT('%', LOWER(:keyword), '%')) OR " +
            "      (:searchType = 'LOCATION' AND LOWER(t.visiting_areas) LIKE CONCAT('%', LOWER(:keyword), '%')))",
            nativeQuery = true)
    long countSearchResults(
            @Param("keyword") String keyword,
            @Param("searchType") String searchType);

    @Query("SELECT COUNT(t) FROM Trainer t")
    long countTrainers();
}
