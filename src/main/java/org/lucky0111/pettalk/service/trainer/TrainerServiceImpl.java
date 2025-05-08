package org.lucky0111.pettalk.service.trainer;

import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.common.UserRole;
import org.lucky0111.pettalk.domain.dto.review.ReviewStatsDTO;
import org.lucky0111.pettalk.domain.dto.trainer.*;
import org.lucky0111.pettalk.domain.entity.trainer.*;
import org.lucky0111.pettalk.domain.entity.common.Tag;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.exception.CustomException;
import org.lucky0111.pettalk.repository.common.TagRepository;
import org.lucky0111.pettalk.repository.review.ReviewRepository;
import org.lucky0111.pettalk.repository.trainer.*;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.lucky0111.pettalk.service.file.FileUploaderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional //DB 작업이 하나의 트랜잭션으로 처리.

public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final PetUserRepository petUserRepository;
    private final CertificationRepository certificationRepository;
    private final TrainerTagRepository trainerTagRepository;
    private final TagRepository tagRepository;
    private final ReviewRepository reviewRepository;
    private final FileUploaderService fileUploaderService;
    private final TrainerPhotoRepository trainerPhotoRepository;
    private final TrainerServiceFeeRepository trainerServiceFeeRepository;

    @Override
    @Transactional(readOnly = true)
    public TrainerDTO getTrainerDetails(String trainerNickname) {
        // 1. Trainer 엔티티 조회 (ID는 UUID)
        Trainer trainer = trainerRepository.findByUser_Nickname(trainerNickname)
                .orElseThrow(() -> new CustomException("훈련사 정보를 찾을 수 없습니다 ID: %s".formatted(trainerNickname), HttpStatus.NOT_FOUND));

        PetUser user = trainer.getUser();

        List<TrainerPhoto> photos = trainer.getPhotos();
        List<TrainerServiceFee> serviceFees = trainer.getServiceFees();

        List<String> specializationNames = getSpecializationNames(trainer.getTrainerId());
        List<CertificationDTO> certificationDtoList = getCertificationDTOList(trainer.getTrainerId());

        List<TrainerPhotoDTO> photoDTOs = getPhotosDTO(photos);
        List<TrainerServiceFeeDTO> serviceFeeDTOs = getServiceFeesDTO(serviceFees);


        ReviewStatsDTO reviewStatsDTO = getReviewStatsDTO(trainer.getTrainerId());


        return new TrainerDTO(
                trainer.getTrainerId(), // UUID 타입
                user != null ? user.getNickname() : null,
                user != null ? user.getProfileImageUrl() : null,
                user != null ? user.getEmail() : null, // email 필드 추가 (PetUser에 있다고 가정)

                trainer.getTitle(),
                trainer.getIntroduction(),
                trainer.getRepresentativeCareer(),
                trainer.getSpecializationText(),
                trainer.getVisitingAreas(),
                trainer.getExperienceYears(),

                photoDTOs,
                serviceFeeDTOs,

                specializationNames, // 태그 이름 목록 (리스트 형태)
                certificationDtoList, // 자격증 DTO 목록
                reviewStatsDTO.averageRating(),
                reviewStatsDTO.reviewCount()

        );
    }
    private List<CertificationDTO> getCertificationDTOList(UUID trainerId){
        List<Certification> certifications = certificationRepository.findByTrainer_TrainerId(trainerId);

        return certifications.stream()
                .map(CertificationDTO::fromEntity)
                .toList();
    }

    // 4. 전문 분야(태그) 목록 조회 (Trainer ID는 UUID)
    private List<String> getSpecializationNames(UUID trainerId){
        List<TrainerTagRelation> trainerTags = trainerTagRepository.findByTrainer_TrainerId(trainerId);
        return trainerTags.stream()
                .map(TrainerTagRelation::getTag) // TrainerTag 엔티티에서 Tag 엔티티를 가져옴 (관계 매핑 필요)
                .map(Tag::getTagName) // Tag 엔티티에서 태그 이름을 가져옴
                .toList();
    }
    // 5. 평점 및 후기 개수 조회 (ReviewRepository 사용, 인자 타입 UUID)
    private ReviewStatsDTO getReviewStatsDTO(UUID trainerId){
        Double averageRating = reviewRepository.findAverageRatingByTrainerId(trainerId);
        Long reviewCount = reviewRepository.countByReviewedTrainerId(trainerId);

        return new ReviewStatsDTO(
                averageRating != null ? averageRating : 0.0, // 평균 평점 (NULL일 경우 0.0)
                reviewCount != null ? reviewCount : 0L  // 후기 개수 (NULL일 경우 0));
        );
    }

    private List<TrainerPhotoDTO> getPhotosDTO(List<TrainerPhoto> photos){
        if(photos == null || photos.isEmpty()){
            return null;
        }
        return photos.stream()
                .map(photo -> new TrainerPhotoDTO(
                        photo.getFileUrl(),
                        photo.getPhotoOrder()
                ))
                .collect(Collectors.toList());
    }

    private List<TrainerServiceFeeDTO> getServiceFeesDTO(List<TrainerServiceFee> serviceFees){
        if(serviceFees == null || serviceFees.isEmpty()){
            return null;
        }
        return serviceFees.stream()
                .map(fee -> new TrainerServiceFeeDTO(
                        fee.getServiceType().name(),
                        fee.getDurationMinutes(),
                        fee.getFeeAmount()
                ))
                .collect(Collectors.toList());
    }


    public void applyTrainer(UUID userId, CertificationRequestDTO certificationDTO, MultipartFile certificationFile) {
        PetUser petUser = findAndValidateUser(userId);

        Trainer trainer = findOrCreateTrainer(userId, petUser);

        SaveSingleCertification(trainer, certificationDTO, certificationFile);

        trainerRepository.save(trainer);
    }

    @Override
    @Transactional
    public void addCertification(UUID trainerId, CertificationRequestDTO certificationDTO, MultipartFile certificationFile) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new CustomException("트레이너를 찾을 수 없습니다 ID: %s".formatted(trainerId), HttpStatus.NOT_FOUND));

        SaveSingleCertification(trainer, certificationDTO, certificationFile);

    }

    private PetUser findAndValidateUser(UUID userId) {
        // PetUserRepository를 사용하여 신청한 userId로 PetUser 엔티티를 조회합니다. (사용자가 없을 경우 예외 처리)
        PetUser petUser = petUserRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다 ID: %s".formatted(userId), HttpStatus.NOT_FOUND));

        //조회된 PetUser의 현재 상태(역할)를 확인하여 이미 훈련사이거나 신청 대기 중이라면 중복 신청 예외를 발생시킵니다.
        if (petUser.getRole() == UserRole.TRAINER || petUser.getRole() == UserRole.ADMIN) {
            throw new DuplicateRequestException("이미 훈련사로 활동 중이거나 관리자입니다.");
        }
        return petUser;
    }
    private Trainer findOrCreateTrainer(UUID userId, PetUser petUser){
        //PetUser에 연결된 Trainer 엔티티가 이미 있는지 조회하고 없으면 생성.
        return trainerRepository.findById(userId) // Trainer ID = User ID (@MapsID)
                .orElseGet(() -> {
                    Trainer newTrainer = new Trainer();
                    newTrainer.setUser(petUser); // PetUser 관계 설정
                    return newTrainer;
                });
    }


    private void SaveSingleCertification(Trainer trainer, CertificationRequestDTO certificationDTO, MultipartFile certificationFile) {

        String fileUrl = null;
        if (certificationFile == null || certificationFile.isEmpty()) {
            throw new CustomException("처리할 자격증 파일이 유효하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
        try {
            String folderName = "certifications/";
            fileUrl = fileUploaderService.uploadFile(certificationFile, folderName);
            Certification certification = new Certification();
            certification.setCertName(certificationDTO.certName());
            certification.setIssuingBody(certificationDTO.issuingBody());
            certification.setIssueDate(certificationDTO.issueDate());
            certification.setFileUrl(fileUrl);
            certification.setApproved(false);

            trainer.addCertification(certification);

            certificationRepository.save(certification);

        } catch (Exception e) {
            // 파일만 업로드 되고 그외 정보 업로드가 안됐을 시 고아파일을 제거.
            e.printStackTrace();
            if (fileUrl != null) {
                try {
                    fileUploaderService.deleteFile(fileUrl);
                } catch (RuntimeException deleteException) {
                    deleteException.printStackTrace();
                }
            }
            throw new CustomException("자격증 처리 및 저장 중 오류 발생: " + e.getMessage(), e, HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

}
