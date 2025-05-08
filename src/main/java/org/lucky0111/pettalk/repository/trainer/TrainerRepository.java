package org.lucky0111.pettalk.repository.trainer;

import org.lucky0111.pettalk.domain.entity.trainer.Trainer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    @Query("SELECT COUNT(t) FROM Trainer t")
    long countTrainers();
}
