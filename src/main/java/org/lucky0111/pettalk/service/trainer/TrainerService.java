package org.lucky0111.pettalk.service.trainer;

import org.lucky0111.pettalk.domain.common.TrainerSearchType;
import org.lucky0111.pettalk.domain.common.TrainerSortType;
import org.lucky0111.pettalk.domain.dto.trainer.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface TrainerService {
    TrainerDTO getTrainerDetails(String trainerNickname);

    TrainerPageDTO searchTrainers(String keyword, TrainerSearchType searchType, int page, int size, TrainerSortType sortType);

    void applyTrainer(UUID userId, CertificationRequestDTO certificationDTO, MultipartFile certificationFile);

    void addCertification(UUID trainerId, CertificationRequestDTO certificationDTO, MultipartFile certificationFile);

    TrainerPageDTO getAllTrainers(int page, int size, TrainerSortType sortType);

    void updateTrainerProfile(UUID authenticatedUserId, UUID trainerId, TrainerProfileUpdateDTO updateDTO, List<MultipartFile> photos);

    TrainerPhotoDTO updateTrainerProfileImage(UUID trainerId, int photoOrder, MultipartFile photo);

    List<TrainerDTO> getRandomTrainers();
}
