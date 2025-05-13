package org.lucky0111.pettalk.domain.dto.trainer;

import java.util.List;

public record TrainerPageDTO (
        List<TrainerDTO> trainerList,
        int pageNo,
        int pageSize,
        int totalPages
){
}
