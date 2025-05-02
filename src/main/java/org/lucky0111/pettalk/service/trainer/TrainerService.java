package org.lucky0111.pettalk.service.trainer;

import org.lucky0111.pettalk.domain.dto.trainer.TrainerDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

public interface TrainerService {
    TrainerDTO getTrainerDetails(UUID trainerId);
}
