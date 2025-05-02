package org.lucky0111.pettalk.repository.user;

import org.lucky0111.pettalk.domain.entity.PetUser;
import org.lucky0111.pettalk.domain.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TrainerRepository extends JpaRepository<Trainer, UUID> {
    Optional<Trainer> findByUser(PetUser user);
}
