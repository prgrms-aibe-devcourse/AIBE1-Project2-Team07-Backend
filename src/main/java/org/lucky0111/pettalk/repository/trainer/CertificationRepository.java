package org.lucky0111.pettalk.repository.trainer;

import org.lucky0111.pettalk.domain.entity.trainer.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByTrainer_TrainerId(UUID trainerId);

    @Query("SELECT c FROM Certification c " +
            "JOIN FETCH c.trainer t " +
            "JOIN FETCH t.user")
    List<Certification> findAllWithTrainerAndUser();
}
