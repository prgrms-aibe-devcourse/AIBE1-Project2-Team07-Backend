package org.lucky0111.pettalk.repository;

import org.lucky0111.pettalk.domain.entity.TrainerTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrainerTagRepository extends JpaRepository<TrainerTag, Long> {
    List<TrainerTag> findByTrainer_TrainerId(UUID trainerId);
}
