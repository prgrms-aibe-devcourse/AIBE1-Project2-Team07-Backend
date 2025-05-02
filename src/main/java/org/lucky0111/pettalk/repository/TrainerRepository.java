package org.lucky0111.pettalk.repository;

import org.lucky0111.pettalk.domain.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, UUID> {
}
