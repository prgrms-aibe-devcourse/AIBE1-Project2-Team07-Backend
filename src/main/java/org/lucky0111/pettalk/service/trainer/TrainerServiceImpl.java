package org.lucky0111.pettalk.service.trainer;

import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.lucky0111.pettalk.domain.common.TrainerSearchType;
import org.lucky0111.pettalk.domain.common.TrainerSortType;
import org.lucky0111.pettalk.domain.common.UserRole;
import org.lucky0111.pettalk.domain.dto.review.ReviewStatsDTO;
import org.lucky0111.pettalk.domain.dto.trainer.*;
import org.lucky0111.pettalk.domain.entity.common.Tag;
import org.lucky0111.pettalk.domain.entity.trainer.*;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.exception.CustomException;
import org.lucky0111.pettalk.repository.common.TagRepository;
import org.lucky0111.pettalk.repository.review.ReviewRepository;
import org.lucky0111.pettalk.repository.trainer.*;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.lucky0111.pettalk.service.file.FileUploaderService;
import org.lucky0111.pettalk.service.mcp.McpService;
import org.lucky0111.pettalk.service.tag.TagService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional //DB 작업이 하나의 트랜잭션으로 처리.
@Log
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
    private final TagService tagService;
    private final McpService mcpService;

    @Override
    @Transactional(readOnly = true)
    public TrainerPageDTO getAllTrainers(int page, int size, TrainerSortType sortType) {
        Pageable pageable = PageRequest.of(page, size);

        List<Trainer> trainers;
        switch (sortType) {
            case REVIEWS:
                trainers = trainerRepository.findAllWithPhotosAndServiceFeesByReviewCount(pageable);
                break;
            case RATING:
                trainers = trainerRepository.findAllWithPhotosAndServiceFeesByRating(pageable);
                break;
            case LATEST:
            default:
                trainers = trainerRepository.findAllWithPhotosAndServiceFeesByLatest(pageable);
                break;
        }

        long totalTrainers = trainerRepository.countTrainers();
        int totalPages = (int) Math.ceil((double) totalTrainers / size);

        List<UUID> trainerIds = trainers.stream()
                .map(Trainer::getTrainerId)
                .collect(Collectors.toList());

        Map<UUID, List<CertificationDTO>> certificationMap = getCertificationMapForTrainers(trainerIds);
        Map<UUID, List<String>> specializationMap = getSpecializationMapForTrainers(trainerIds);
        Map<UUID, ReviewStatsDTO> reviewStatsMap = getReviewStatsMapForTrainers(trainerIds);

        List<TrainerDTO> trainerDTOs = trainers.stream()
                .map(trainer -> convertToTrainerDTO(
                        trainer,
                        getPhotosDTO(trainer.getPhotos()),
                        getServiceFeesDTO(trainer.getServiceFees()),
                        specializationMap.getOrDefault(trainer.getTrainerId(), Collections.emptyList()),
                        certificationMap.getOrDefault(trainer.getTrainerId(), Collections.emptyList()),
                        reviewStatsMap.getOrDefault(trainer.getTrainerId(), new ReviewStatsDTO(0.0, 0L))
                ))
                .collect(Collectors.toList());

        return new TrainerPageDTO(trainerDTOs, page, size, totalPages);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerDTO> getRandomTrainers() {
        List<Trainer> randomTrainers = trainerRepository.findRandomTrainers();

        List<UUID> trainerIds = randomTrainers.stream()
                .map(Trainer::getTrainerId)
                .collect(Collectors.toList());

        Map<UUID, List<CertificationDTO>> certificationMap = getCertificationMapForTrainers(trainerIds);
        Map<UUID, List<String>> specializationMap = getSpecializationMapForTrainers(trainerIds);
        Map<UUID, ReviewStatsDTO> reviewStatsMap = getReviewStatsMapForTrainers(trainerIds);

        List<TrainerDTO> trainerDTOs = randomTrainers.stream()
                .map(trainer -> convertToTrainerDTO(
                        trainer,
                        getPhotosDTO(trainer.getPhotos()),
                        getServiceFeesDTO(trainer.getServiceFees()),
                        specializationMap.getOrDefault(trainer.getTrainerId(), Collections.emptyList()),
                        certificationMap.getOrDefault(trainer.getTrainerId(), Collections.emptyList()),
                        reviewStatsMap.getOrDefault(trainer.getTrainerId(), new ReviewStatsDTO(0.0, 0L))
                ))
                .collect(Collectors.toList());
        return trainerDTOs;
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerPageDTO searchTrainers(String keyword, TrainerSearchType searchType, int page, int size, TrainerSortType sortType) {
        int offset = page * size;

        List<Trainer> trainers = trainerRepository.searchTrainersWithSort(
                keyword,
                searchType != null ? searchType.name() : "ALL",
                sortType != null ? sortType.name() : "LATEST",
                size,
                offset);

        long totalResults = trainerRepository.countSearchResults(
                keyword,
                searchType != null ? searchType.name() : "ALL");

        int totalPages = (int) Math.ceil((double) totalResults / size);

        List<UUID> trainerIds = trainers.stream()
                .map(Trainer::getTrainerId)
                .collect(Collectors.toList());

        Map<UUID, List<CertificationDTO>> certificationMap = getCertificationMapForTrainers(trainerIds);
        Map<UUID, List<String>> specializationMap = getSpecializationMapForTrainers(trainerIds);
        Map<UUID, ReviewStatsDTO> reviewStatsMap = getReviewStatsMapForTrainers(trainerIds);

        List<TrainerDTO> trainerDTOs = trainers.stream()
                .map(trainer -> convertToTrainerDTO(
                        trainer,
                        getPhotosDTO(trainer.getPhotos()),
                        getServiceFeesDTO(trainer.getServiceFees()),
                        specializationMap.getOrDefault(trainer.getTrainerId(), Collections.emptyList()),
                        certificationMap.getOrDefault(trainer.getTrainerId(), Collections.emptyList()),
                        reviewStatsMap.getOrDefault(trainer.getTrainerId(), new ReviewStatsDTO(0.0, 0L))
                ))
                .collect(Collectors.toList());

        return new TrainerPageDTO(trainerDTOs, page, size, totalPages);
    }


    @Override
    @Transactional
    public void updateTrainerProfile(UUID authenticatedUserId, UUID trainerId, TrainerProfileUpdateDTO updateDTO, List<MultipartFile> photos) {

        List<String> uploadedFileUrls = new ArrayList<>();

        try {
            if (!authenticatedUserId.equals(trainerId)) {
                throw new CustomException("자신의 프로필만 수정할 수 있습니다.", HttpStatus.FORBIDDEN); // 403 Forbidden
            }
            // N+1 문제를 해결하기 위해 trainer 한번에 로딩하는 메소드 필요
            Trainer trainer = trainerRepository.findByIdWithProfileCollections(trainerId)
                    .orElseThrow(() -> new CustomException("트레이너를 찾을 수 없습니다 ID: %s".formatted(trainerId), HttpStatus.NOT_FOUND));

            trainer.setTitle(updateDTO.title());
            trainer.setIntroduction(updateDTO.introduction());
            trainer.setRepresentativeCareer(updateDTO.representativeCareer());
            trainer.setSpecializationText(updateDTO.specializationText());
            trainer.setVisitingAreas(updateDTO.visitingAreas());

            updateTrainerServiceFees(trainer, updateDTO.serviceFees());
            if (!photos.isEmpty()) {
                uploadedFileUrls = updateTrainerPhotos(trainer, photos);
            }
            updateTrainerTags(trainer, updateDTO.representativeCareer(), updateDTO.specializationText(), updateDTO.introduction());

            trainerRepository.save(trainer);
        } catch (Exception e) {
            if (!uploadedFileUrls.isEmpty()) { // 업로드 성공한 파일이 있다면
                uploadedFileUrls.forEach(url -> {
                    try {
                        fileUploaderService.deleteFile(url);
                    } catch (RuntimeException deleteException) {
                        deleteException.printStackTrace();
                    }
                });
            }
            throw new CustomException("트레이너 프로필 업데이트 중 최종 오류 발생: " + e.getMessage(), e, HttpStatus.INTERNAL_SERVER_ERROR); // 원본 예외 포함
        }

    }

    private void updateTrainerServiceFees(Trainer trainer, List<ServiceFeeUpdateDTO> serviceFeeDTOs) {
        if (trainer.getServiceFees() != null) { // 컬렉션이 null일 경우 체크 (findByIdWithCollections가 잘 로딩하면 필요 없을 수 있음)
            trainer.getServiceFees().clear();
        } else {
            trainer.setServiceFees(new HashSet<>());
        }
        // 2. updateDTO에 있는 ServiceFeeUpdateDTO 목록을 TrainerServiceFee 엔티티로 변환하여 Trainer 컬렉션에 추가
        if (serviceFeeDTOs != null && !serviceFeeDTOs.isEmpty()) { // 비어있지 않은 목록일 경우에만 처리
            for (ServiceFeeUpdateDTO feeDTO : serviceFeeDTOs) {
                TrainerServiceFee newFee = new TrainerServiceFee();
                newFee.setServiceType(feeDTO.serviceType());
                newFee.setDurationMinutes(feeDTO.time());
                newFee.setFeeAmount(feeDTO.price());

                trainer.addServiceFee(newFee);
            }
        }
    }

    private List<String> updateTrainerPhotos(Trainer trainer, List<MultipartFile> photos) throws IOException {
        if (photos == null || photos.size() != 2) {
            throw new CustomException("프로필 사진은 정확히 2장을 첨부해야 합니다.", HttpStatus.BAD_REQUEST);
        }
        deleteExistingTrainerPhotos(trainer);

        List<String> uploadedFileUrls = new ArrayList<>();

        int photoOrder = 0; // 사진 순서 (0부터 시작)

        for (MultipartFile photoFile : photos) {
            if (photoFile.isEmpty()) {
                throw new CustomException("첨부된 사진 파일 중 비어있는 파일이 있습니다.", HttpStatus.BAD_REQUEST);
            }
            // 파일 업로드 (S3)
            String folderName = "trainer-photos/" + trainer.getTrainerId() + "/"; // 트레이너 ID별 폴더
            String fileUrl = fileUploaderService.uploadFile(photoFile, folderName);
            uploadedFileUrls.add(fileUrl); // 업로드 성공한 URL 추적

            TrainerPhoto newPhoto = new TrainerPhoto();
            newPhoto.setFileUrl(fileUrl);
            newPhoto.setPhotoOrder(photoOrder++);

            trainer.addPhoto(newPhoto);
        }
        return uploadedFileUrls;
    }

    private void updateTrainerTags(Trainer trainer, String career, String specializationText, String introduction) throws Exception {
        List<String> recommendedTagNames = mcpService.makeTagListForTrainer(specializationText, career, introduction);
        Set<String> uniqueRecommendedTagNames = new HashSet<>(recommendedTagNames);

        List<Long> recommendedTagIds = new ArrayList<>();
        if (!uniqueRecommendedTagNames.isEmpty()) {
            for (String tagName : uniqueRecommendedTagNames) {
                Optional<Tag> tagOptional = tagRepository.findByTagName(tagName); // TagRepository 사용
                if (tagOptional.isPresent()) {
                    recommendedTagIds.add(tagOptional.get().getTagId()); // Tag 엔티티에서 ID 가져오기 (getId() 또는 getTagId() 확인)
                } else {
                    log.warning("Warning: 태그이름 : %s 이 테이블에 존재하지 않아 무시합니다.".formatted(tagName)); // 경고 로깅
                }
            }
        }
        trainerTagRepository.updateTrainerTags(trainer.getTrainerId(), recommendedTagIds);
    }


    private void deleteExistingTrainerPhotos(Trainer trainer) {
        Set<TrainerPhoto> existingPhotos = trainer.getPhotos();

        if (existingPhotos != null && !existingPhotos.isEmpty()) {
            existingPhotos.forEach(photo -> {
                try {
                    fileUploaderService.deleteFile(photo.getFileUrl());
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    throw new CustomException("기존 프로필 사진 S3 삭제 중 오류 발생: ", e, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            });
            trainer.getPhotos().clear();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerDTO getTrainerDetails(String trainerNickname) {
        // 1. Trainer 엔티티 조회 (ID는 UUID)
        Trainer trainer = trainerRepository.findByUser_Nickname(trainerNickname)
                .orElseThrow(() -> new CustomException("훈련사 정보를 찾을 수 없습니다 ID: %s".formatted(trainerNickname), HttpStatus.NOT_FOUND));

        Set<TrainerPhoto> photos = trainer.getPhotos();
        Set<TrainerServiceFee> serviceFees = trainer.getServiceFees();

        List<String> specializationNames = getSpecializationNames(trainer.getTrainerId());
        List<CertificationDTO> certificationDtoList = getCertificationDTOList(trainer.getTrainerId());

        List<TrainerPhotoDTO> photoDTOs = getPhotosDTO(photos);
        List<TrainerServiceFeeDTO> serviceFeeDTOs = getServiceFeesDTO(serviceFees);


        ReviewStatsDTO reviewStatsDTO = getReviewStatsDTO(trainer.getTrainerId());

        return convertToTrainerDTO(
                trainer,
                photoDTOs,
                serviceFeeDTOs,
                specializationNames,
                certificationDtoList,
                reviewStatsDTO
        );
    }

    private Map<UUID, List<CertificationDTO>> getCertificationMapForTrainers(List<UUID> trainerIds) {
        // 트레이너 ID 목록으로 자격증 조회
        return certificationRepository.findAllByTrainer_TrainerIdIn(trainerIds).stream()
                .collect(Collectors.groupingBy(
                        certification -> certification.getTrainer().getTrainerId(),
                        Collectors.mapping(CertificationDTO::fromEntity, Collectors.toList())
                ));
    }

    private Map<UUID, List<String>> getSpecializationMapForTrainers(List<UUID> trainerIds) {
        return trainerTagRepository.findAllByTrainer_TrainerIdIn(trainerIds).stream()
                .collect(Collectors.groupingBy(
                        relation -> relation.getTrainer().getTrainerId(),
                        Collectors.mapping(
                                relation -> relation.getTag().getTagName(),
                                Collectors.toList()
                        )
                ));
    }

    // 여러 트레이너의 리뷰 통계를 한 번에 조회
    private Map<UUID, ReviewStatsDTO> getReviewStatsMapForTrainers(List<UUID> trainerIds) {
        // 트레이너 ID 목록으로 평균 평점 조회
        Map<UUID, Double> avgRatings = reviewRepository.findAverageRatingsByTrainerIds(trainerIds);

        // 트레이너 ID 목록으로 리뷰 개수 조회
        Map<UUID, Long> reviewCounts = reviewRepository.countReviewsByTrainerIds(trainerIds);

        // 결과 맵 생성
        Map<UUID, ReviewStatsDTO> result = new HashMap<>();
        for (UUID trainerId : trainerIds) {
            Double avgRating = avgRatings.getOrDefault(trainerId, 0.0);
            Long reviewCount = reviewCounts.getOrDefault(trainerId, 0L);
            result.put(trainerId, new ReviewStatsDTO(avgRating, reviewCount));
        }

        return result;
    }

    // TrainerDTO 변환 메서드 분리
    private TrainerDTO convertToTrainerDTO(
            Trainer trainer,
            List<TrainerPhotoDTO> photoDTOs,
            List<TrainerServiceFeeDTO> serviceFeeDTOs,
            List<String> specializationNames,
            List<CertificationDTO> certificationDtoList,
            ReviewStatsDTO reviewStatsDTO) {

        PetUser user = trainer.getUser();

        return new TrainerDTO(
                trainer.getTrainerId(),
                user != null ? user.getName() : null,
                user != null ? user.getNickname() : null,
                user != null ? user.getProfileImageUrl() : null,
                user != null ? user.getEmail() : null,
                trainer.getTitle(),
                trainer.getIntroduction(),
                trainer.getRepresentativeCareer(),
                trainer.getSpecializationText(),
                trainer.getVisitingAreas(),
                photoDTOs,
                serviceFeeDTOs,
                specializationNames,
                certificationDtoList,
                reviewStatsDTO.averageRating(),
                reviewStatsDTO.reviewCount()
        );
    }

    private List<CertificationDTO> getCertificationDTOList(UUID trainerId) {
        List<Certification> certifications = certificationRepository.findByTrainer_TrainerId(trainerId);

        return certifications.stream()
                .map(CertificationDTO::fromEntity)
                .toList();
    }

    // 4. 전문 분야(태그) 목록 조회 (Trainer ID는 UUID)
    private List<String> getSpecializationNames(UUID trainerId) {
        List<TrainerTagRelation> trainerTags = trainerTagRepository.findByTrainer_TrainerId(trainerId);
        return trainerTags.stream()
                .map(TrainerTagRelation::getTag) // TrainerTag 엔티티에서 Tag 엔티티를 가져옴 (관계 매핑 필요)
                .map(Tag::getTagName) // Tag 엔티티에서 태그 이름을 가져옴
                .toList();
    }

    // 5. 평점 및 후기 개수 조회 (ReviewRepository 사용, 인자 타입 UUID)
    private ReviewStatsDTO getReviewStatsDTO(UUID trainerId) {
        Double averageRating = reviewRepository.findAverageRatingByTrainerId(trainerId);
        Long reviewCount = reviewRepository.countByReviewedTrainerId(trainerId);

        return new ReviewStatsDTO(
                averageRating != null ? averageRating : 0.0, // 평균 평점 (NULL일 경우 0.0)
                reviewCount != null ? reviewCount : 0L  // 후기 개수 (NULL일 경우 0));
        );
    }

    private List<TrainerPhotoDTO> getPhotosDTO(Set<TrainerPhoto> photos) {
        if (photos == null || photos.isEmpty()) {
            return Collections.emptyList();
        }

        return photos.stream()
                .map(photo -> new TrainerPhotoDTO(
                        photo.getFileUrl(),
                        photo.getPhotoOrder()
                ))
                .collect(Collectors.toList());
    }

    private List<TrainerServiceFeeDTO> getServiceFeesDTO(Set<TrainerServiceFee> serviceFees) {
        if (serviceFees == null || serviceFees.isEmpty()) {
            return Collections.emptyList();
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

    private Trainer findOrCreateTrainer(UUID userId, PetUser petUser) {
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
            String folderName = "certifications/" + trainer.getTrainerId() + "/";
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
