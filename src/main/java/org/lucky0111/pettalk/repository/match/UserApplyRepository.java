package org.lucky0111.pettalk.repository.match;

import org.lucky0111.pettalk.domain.common.ApplyStatus;
import org.lucky0111.pettalk.domain.entity.match.UserApply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserApplyRepository extends JpaRepository<UserApply, Long> {
    boolean existsByPetUser_userIdAndTrainer_trainerIdAndApplyStatus(UUID petUserId, UUID trainerId, ApplyStatus applyStatus);

    List<UserApply> findByTrainer_TrainerId(UUID trainerTrainerId);

    @Query("SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE ua.applyId = :applyId")
    Optional<UserApply> findByIdWithRelations(@Param("applyId") Long applyId);

    @Query("SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE ua.petUser.userId = :userId " +
            "ORDER BY ua.createdAt DESC")
    List<UserApply> findByPetUser_UserIdWithRelations(@Param("userId") UUID userId);

    @Query("SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE ua.petUser.userId = :userId " +
            "AND ua.applyStatus = :status " +
            "ORDER BY ua.createdAt DESC")
    List<UserApply> findByPetUser_UserIdAndApplyStatusWithRelations(
            @Param("userId") UUID userId,
            @Param("status") ApplyStatus applyStatus);

    @Query("SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE t.trainerId = :trainerId " +
            "AND ua.applyStatus = :status " +
            "ORDER BY ua.createdAt DESC")
    List<UserApply> findByTrainer_TrainerIdAndApplyStatusWithRelations(
            @Param("trainerId") UUID trainerId,
            @Param("status") ApplyStatus applyStatus);

    @Query(value = "SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE ua.petUser.userId = :userId",
            countQuery = "SELECT COUNT(ua) FROM UserApply ua WHERE ua.petUser.userId = :userId")
    Page<UserApply> findByPetUser_UserIdWithRelationsPaged(
            @Param("userId") UUID userId,
            Pageable pageable);

    @Query(value = "SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE t.trainerId = :trainerId",
            countQuery = "SELECT COUNT(ua) FROM UserApply ua JOIN ua.trainer t WHERE t.trainerId = :trainerId")
    Page<UserApply> findByTrainer_TrainerIdWithRelationsPaged(
            @Param("trainerId") UUID trainerId,
            Pageable pageable);

    @Query("SELECT ua.applyId, " +
            "(SELECT COUNT(r) FROM Review r WHERE r.userApply.applyId = ua.applyId) > 0 " +
            "FROM UserApply ua " +
            "WHERE ua.applyId IN :applyIds")
    List<Object[]> checkReviewExistenceByApplyIds(@Param("applyIds") List<Long> applyIds);

    @Query(value = "SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE ua.petUser.userId = :userId " +
            "AND ua.applyStatus = :status",
            countQuery = "SELECT COUNT(ua) FROM UserApply ua " +
                    "WHERE ua.petUser.userId = :userId AND ua.applyStatus = :status")
    Page<UserApply> findByPetUser_UserIdAndStatusWithRelationsPaged(
            @Param("userId") UUID userId,
            @Param("status") ApplyStatus applyStatus,
            Pageable pageable);

    @Query(value = "SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE t.trainerId = :trainerId " +
            "AND ua.applyStatus = :status",
            countQuery = "SELECT COUNT(ua) FROM UserApply ua " +
                    "JOIN ua.trainer t WHERE t.trainerId = :trainerId AND ua.applyStatus = :status")
    Page<UserApply> findByTrainer_TrainerIdAndStatusWithRelationsPaged(
            @Param("trainerId") UUID trainerId,
            @Param("status") ApplyStatus applyStatus,
            Pageable pageable);

}
