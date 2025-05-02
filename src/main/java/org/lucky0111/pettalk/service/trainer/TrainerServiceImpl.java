package org.lucky0111.pettalk.service.trainer;

import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.dto.trainer.CertificationDTO;
import org.lucky0111.pettalk.domain.dto.trainer.TrainerDTO;
import org.lucky0111.pettalk.domain.entity.*;
import org.lucky0111.pettalk.exception.CustomException;
import org.lucky0111.pettalk.repository.common.TagRepository;
import org.lucky0111.pettalk.repository.review.ReviewRepository;
import org.lucky0111.pettalk.repository.trainer.CertificationRepository;
import org.lucky0111.pettalk.repository.trainer.TrainerRepository;
import org.lucky0111.pettalk.repository.trainer.TrainerTagRepository;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final PetUserRepository petUserRepository; // PetUser Repository
    private final CertificationRepository certificationRepository; // 자격증 Repository
    private final TrainerTagRepository trainerTagRepository; // TrainerTag Repository
    private final TagRepository tagRepository; // Tag Repository (TrainerTag에서 Tag 정보를 가져오기 위해 필요)
    private final ReviewRepository reviewRepository; // Review Repository

    @Override
    public TrainerDTO getTrainerDetails(UUID trainerId) { // 인자 타입 UUID
        // 1. Trainer 엔티티 조회 (ID는 UUID)
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new CustomException("Trainer not found with id: %s".formatted(trainerId), HttpStatus.NOT_FOUND));

        // 2. 연관된 PetUser 엔티티 조회 (Trainer 엔티티에 User user 필드가 있고 @OneToOne 관계로 매핑되었다고 가정)
        PetUser user = trainer.getUser();

        // 3. 자격증 목록 조회 (Trainer ID는 UUID)
        List<Certification> certifications = certificationRepository.findByTrainer_TrainerId(trainerId);
        List<CertificationDTO> certificationDtoList = certifications.stream()
                .map(CertificationDTO::fromEntity) // Certification 엔티티 -> CertificationDto Record 변환 (CertificationDto에 fromEntity 메소드 구현 필요)
                .toList();

        // 4. 전문 분야(태그) 목록 조회 (Trainer ID는 UUID)
        List<TrainerTagRelation> trainerTags = trainerTagRepository.findByTrainer_TrainerId(trainerId);
        List<String> specializationNames = trainerTags.stream()
                .map(TrainerTagRelation::getTag) // TrainerTag 엔티티에서 Tag 엔티티를 가져옴 (관계 매핑 필요)
                .map(Tag::getTagName) // Tag 엔티티에서 태그 이름을 가져옴
                .toList();

        // 5. 평점 및 후기 개수 조회 (ReviewRepository 사용, 인자 타입 UUID)
        Double averageRating = reviewRepository.findAverageRatingByTrainerId(trainerId);
        Long reviewCount = reviewRepository.countByReviewedTrainerId(trainerId);

        // 6. 엔티티 -> DTO (Record) 매핑 및 반환
        return new TrainerDTO(
                trainer.getTrainerId(), // UUID 타입
                user != null ? user.getNickname() : null,
                user != null ? user.getProfileImageUrl() : null,
                user != null ? user.getEmail() : null, // email 필드 추가 (PetUser에 있다고 가정)
                trainer.getIntroduction(),
                trainer.getExperienceYears(),
                specializationNames, // 태그 이름 목록
                certificationDtoList, // 자격증 DTO 목록
                averageRating != null ? averageRating : 0.0, // 평균 평점 (NULL일 경우 0.0)
                reviewCount != null ? reviewCount : 0L // 후기 개수 (NULL일 경우 0)
        );
    }
}
