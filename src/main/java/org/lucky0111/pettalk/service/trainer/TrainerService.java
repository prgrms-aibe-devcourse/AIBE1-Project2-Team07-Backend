package org.lucky0111.pettalk.service.trainer;

import org.lucky0111.pettalk.domain.dto.trainer.TrainerApplicationRequestDTO;
import org.lucky0111.pettalk.domain.dto.trainer.TrainerDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface TrainerService {
    TrainerDTO getTrainerDetails(String trainerId);

    // 사용자 ID, 신청 정보, 자격증 파일 목록을 받아 훈련사 신청 처리
    void applyTrainer(UUID userId, TrainerApplicationRequestDTO applicationReq, List<MultipartFile> certificationFiles);
    // 신청제출 결과에 대한 정보만 -> 반환값없음
    // 승인은 추후에 이뤄지는 비동기적인 처리

}
