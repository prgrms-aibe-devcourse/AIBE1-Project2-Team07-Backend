package org.lucky0111.pettalk.repository.trainer;

import org.lucky0111.pettalk.domain.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByTrainer_TrainerId(UUID trainerId);
}
