package org.lucky0111.pettalk.service.trainer;

import org.lucky0111.pettalk.domain.dto.trainer.CertificationRequestDTO;
import org.lucky0111.pettalk.domain.dto.trainer.TrainerDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface TrainerService {
    TrainerDTO getTrainerDetails(String trainerNickname);

    // 사용자 ID, 신청 정보, 자격증 파일 목록을 받아 훈련사 신청 처리
    void applyTrainer(UUID userId, CertificationRequestDTO certificationDTO, MultipartFile certificationFile);
    // 승인은 추후에 이뤄지는 비동기적인 처리

    void addCertification(UUID trainerId, CertificationRequestDTO certificationDTO, MultipartFile certificationFile);

}
