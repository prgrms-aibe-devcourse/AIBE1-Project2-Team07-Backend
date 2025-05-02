package org.lucky0111.pettalk.repository.match;

import org.lucky0111.pettalk.domain.common.Status;
import org.lucky0111.pettalk.domain.entity.PetUser;
import org.lucky0111.pettalk.domain.entity.Trainer;
import org.lucky0111.pettalk.domain.entity.match.UserApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public interface UserApplyRepository extends JpaRepository<UserApply, Long> {
    List<UserApply> findByPetUser(PetUser petUser);
    List<UserApply> findByTrainer(Trainer trainer);
    List<UserApply> findByPetUserAndStatus(PetUser petUser, Status status);
    List<UserApply> findByTrainerAndStatus(Trainer trainer, Status status);

    List<UserApply> findByPetUser_UserId(UUID petUserId);

    boolean existsByPetUser_userIdAndTrainer_trainerIdAndStatus(UUID petUserId, UUID trainerId, Status status);

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
            "WHERE ua.petUser.userId = :userId")
    List<UserApply> findByPetUser_UserIdWithRelations(@Param("userId") UUID userId);

    @Query("SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE t.trainerId = :trainerId")
    List<UserApply> findByTrainer_TrainerIdWithRelations(@Param("trainerId") UUID trainerId);

    @Query("SELECT ua.applyId, " +
            "(SELECT COUNT(r) FROM Review r WHERE r.userApply.applyId = ua.applyId) > 0 " +
            "FROM UserApply ua " +
            "WHERE ua.applyId IN :applyIds")
    List<Object[]> checkReviewExistenceByApplyIds(@Param("applyIds") List<Long> applyIds);

    default Map<Long, Boolean> hasReviewByApplyIds(List<Long> applyIds) {
        return checkReviewExistenceByApplyIds(applyIds).stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Boolean) arr[1]
                ));
    }
}
