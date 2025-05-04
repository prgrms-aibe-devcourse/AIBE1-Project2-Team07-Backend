package org.lucky0111.pettalk.service.trainer;

import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.dto.trainer.CertificationDTO;
import org.lucky0111.pettalk.domain.dto.trainer.CertificationRequestDTO;
import org.lucky0111.pettalk.domain.dto.trainer.TrainerApplicationRequestDTO;
import org.lucky0111.pettalk.domain.dto.trainer.TrainerDTO;
import org.lucky0111.pettalk.domain.entity.trainer.Trainer;
import org.lucky0111.pettalk.domain.entity.common.Tag;
import org.lucky0111.pettalk.domain.entity.trainer.Certification;
import org.lucky0111.pettalk.domain.entity.trainer.TrainerTagRelation;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.exception.CustomException;
import org.lucky0111.pettalk.repository.common.TagRepository;
import org.lucky0111.pettalk.repository.review.ReviewRepository;
import org.lucky0111.pettalk.repository.trainer.CertificationRepository;
import org.lucky0111.pettalk.repository.trainer.TrainerRepository;
import org.lucky0111.pettalk.repository.trainer.TrainerTagRepository;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional //메소드 위에 @Transactional 어노테이션을 붙여 DB 작업이 하나의 트랜잭션으로 처리되도록 합니다.

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

    @Override
    public void applyTrainer(UUID userId, TrainerApplicationRequestDTO applicationReq, List<MultipartFile> certificationFiles) {
        // PetUserRepository를 사용하여 신청한 userId로 PetUser 엔티티를 조회합니다. (사용자가 없을 경우 예외 처리)
        PetUser petUser = petUserRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found with id: %s".formatted(userId), HttpStatus.NOT_FOUND));
        //조회된 PetUser의 현재 상태(역할)를 확인하여 이미 훈련사이거나 신청 대기 중이라면 중복 신청 예외를 발생시킵니다.
        if(petUser.getRole().equals("trainer") || petUser.getRole().equals("admin")) {
            throw new DuplicateRequestException("User has already applied or already a trainer");
        }
        //TrainerRepository를 사용하여 해당 PetUser에 연결된 Trainer 엔티티가 이미 있는지 조회합니다.
        Trainer trainer = trainerRepository.findById(userId) // Trainer ID = User ID (@MapsID)
                .orElseGet(() -> {

        //Trainer 엔티티 생성 또는 업데이트:
        //만약 Trainer 엔티티가 없다면 새로 생성하고 PetUser 엔티티와 연결합니다 (trainer.setUser(petUser);). @MapsId 패턴이라면 trainer.setTrainerId(userId); 와 같이 ID를 설정해야 할 수도 있습니다.

                    Trainer newTrainer = new Trainer();
                    newTrainer.setUser(petUser); // PetUser 관계 설정
                    return newTrainer;

                });
        //TrainerApplicationReq의 정보(introduction, experienceYears 등)로 Trainer 엔티티 필드를 채웁니다.
        trainer.setIntroduction(applicationReq.introduction());
        trainer.setExperienceYears(applicationReq.experienceYears());

        //자격증 파일 업로드 및 Certification 엔티티 생성:
        //주입받은 FileUploaderService를 사용하여 certificationFiles 목록의 각 MultipartFile을 업로드하고 해당 파일의 URL을 받습니다.

        //TrainerApplicationReq의 certifications 목록을 순회하면서, 각 CertificationReq 정보와 업로드된 파일 URL을 사용하여 Certification 엔티티를 새로 생성합니다.

        //파일이 있는지 먼저 검증
        List<CertificationRequestDTO> certRequest = applicationReq.certifications();

        if(certRequest != null && !certRequest.isEmpty()) {
            // 있다면 자격증 DTO 목록과 파일목록의 개수가 일치하는지 검증
            if(certRequest.size() != certificationFiles.size()) {
                // 개수 불일치 시 예외처리
                throw new IllegalArgumentException("자격증 정보와 파일개수가 일치하지 않습니다.");
            }
            // 자격증 목록 순회하며 처리
            for(int i = 0; i < certRequest.size(); i++) {
                // 현재 순회중인 자격증 정보 DTO와 파일 가져오기
                CertificationRequestDTO certReqDTO = certRequest.get(i);
                MultipartFile certificationFile = certificationFiles.get(i);

                // 자격증 파일 업로드
                String fileUrl = null;
//                try{
//                    // FileUploaderService를 사용해 외부 스토리지에 업로드 및 접근 가능한 URL받음
//                    fileUrl = fileUploaderService.uploadFile(certificationFile);
//                } catch (IOException e){
//                    throw new RuntimeException("파일 업로드 중 오류 발생 : "+ certificationFile.getOriginalFilename(),e);
//                } 구현 예정

                // Certification 엔티티 생성

                // 기본 키 (certId - Long 타입)는 @GeneratedValue(strategy = GenerationType.IDENTITY)에 의해 DB에서 자동 생성되므로 코드에서 설정하지 않습니다.
                Certification certification = new Certification();

                //생성된 Certification 엔티티를 위에서 만든 Trainer 엔티티와 연결합니다 (certification.setTrainer(trainer);).
                // 생성된 Certification 엔티티와 Trainer 엔티티를 연결 (N:1 관계 설정)
                // Certification 엔티티에 @ManyToOne Trainer trainer; 필드가 있다고 가정
                certification.setTrainer(trainer);

                // CertificationRequest DTO에서 나머지 정보 가져와서 엔티티에 설정
                certification.setCertName(certReqDTO.certName()); // Record의 접근자 메소드 사용
                certification.setIssuingBody(certReqDTO.issuingBody()); // Record의 접근자 메소드 사용
                certification.setIssueDate(certReqDTO.issueDate()); // Record의 접근자 메소드 사용 (LocalDate 타입)

                // 업로드된 파일 URL 설정
                certification.setFileUrl(fileUrl);

                // 초기 승인 상태 설정 (관리자 승인 전이므로 false)
                //Certification 엔티티의 approved 필드는 기본값인 false로 설정합니다.
                certification.setApproved(false); // boolean 또는 Boolean 타입이라고 가정

                //데이터 저장:
                //생성된 각 Certification 엔티티를 certificationRepository.save(certification); 로 저장합니다.
                // 생성된 Certification 엔티티를 데이터베이스에 저장
                certificationRepository.save(certification);
            }
        }
                //변경된 Trainer 엔티티를 trainerRepository.save(trainer);로 저장합니다. (새로 생성된 경우 삽입, 기존 엔티티라면 업데이트)
            trainerRepository.save(trainer);

         //PetUser 역할 업데이트: PetUser 엔티티의 role 필드를 'trainer' 또는 그에 준하는 상태로 업데이트하고 petUserRepository.save(petUser);로 저장합니다.
        petUser.setRole("trainer");
        petUserRepository.save(petUser);

    }
}
