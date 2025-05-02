package org.lucky0111.pettalk.repository.trainer;

import org.lucky0111.pettalk.domain.entity.TrainerTagRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrainerTagRepository extends JpaRepository<TrainerTagRelation, Long> {
    List<TrainerTagRelation> findByTrainer_TrainerId(UUID trainerId);
}
