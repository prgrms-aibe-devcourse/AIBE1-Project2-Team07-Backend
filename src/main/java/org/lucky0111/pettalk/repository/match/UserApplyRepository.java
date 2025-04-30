package org.lucky0111.pettalk.repository.match;

import org.lucky0111.pettalk.domain.common.Status;
import org.lucky0111.pettalk.domain.entity.PetUser;
import org.lucky0111.pettalk.domain.entity.Trainer;
import org.lucky0111.pettalk.domain.entity.match.UserApply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserApplyRepository extends JpaRepository<UserApply, Long> {
    List<UserApply> findByPetUser(PetUser petUser);
    List<UserApply> findByTrainer(Trainer trainer);
    List<UserApply> findByPetUserAndStatus(PetUser petUser, Status status);
    List<UserApply> findByTrainerAndStatus(Trainer trainer, Status status);

    List<UserApply> findByPetUser_UserId(UUID petUserId);

    boolean existsByPetUser_userIdAndTrainer_trainerIdAndStatus(UUID petUserId, UUID trainerId, Status status);

    List<UserApply> findByTrainer_TrainerId(UUID trainerTrainerId);
}
