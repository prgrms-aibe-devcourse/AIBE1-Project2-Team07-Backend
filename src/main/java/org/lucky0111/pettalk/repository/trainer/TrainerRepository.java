package org.lucky0111.pettalk.repository.trainer;

import org.lucky0111.pettalk.domain.entity.trainer.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TrainerRepository extends JpaRepository<Trainer, UUID> {
}
