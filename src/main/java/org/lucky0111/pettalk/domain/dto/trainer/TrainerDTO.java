package org.lucky0111.pettalk.domain.dto.trainer;

import org.lucky0111.pettalk.domain.entity.trainer.Trainer;

import java.util.List;
import java.util.UUID;

public record TrainerDTO(
        UUID trainerId, // Trainer의 ID는 UUID

        String nickname, // PetUser에서 가져옴
        String profileImageUrl, // PetUser에서 가져옴
        String email, // PetUser에서 가져옴

        String title,
        String introduction, // Trainer에서 가져옴
        String representativeCareer, // 대표 경력
        String specializationText, // 예: "행동 교정, 아질리티, 기본 복종"
        String visitingAreas, // 예: "강남구, 서초구, 송파구, 분당"
        int experienceYears, // Trainer에서 가져옴

        List<TrainerPhotoDTO> photos,
        List<TrainerServiceFeeDTO> serviceFees,

        List<String> specializations, // TrainerTag,Tag 조인 결과 (태그 이름 목록)
        List<CertificationDTO> certifications, // Certification 목록
        double averageRating, // Review에서 계산
        long reviewCount // Review에서 계산
) {}
