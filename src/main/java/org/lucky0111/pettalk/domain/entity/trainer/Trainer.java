package org.lucky0111.pettalk.domain.entity.trainer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.entity.user.PetUser;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "trainers")
@NoArgsConstructor
public class Trainer extends BaseTimeEntity {
    @Id
    private UUID trainerId; // FK -> PetUser.userId

    @OneToOne
    @MapsId
    @JoinColumn(name = "trainer_id")
    private PetUser user;

    private LocalDateTime approvedAt;

    private String title;
    @Column(length = 1000)
    private String representativeCareer; // 대표 경력
    private String specializationText; // 예: "행동 교정, 아질리티, 기본 복종"
    private String visitingAreas; // 예: "강남구, 서초구, 송파구, 분당"
    @Column(length = 1000)
    private String introduction; // 자기소개

    // orphanRemoval = true: Trainer에서 Certification 연결이 끊어지면 해당 Certification 엔티티를 DB에서 삭제
    @OneToMany(mappedBy = "trainer", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<Certification> certifications = new ArrayList<>();

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TrainerPhoto> photos = new HashSet<>();

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TrainerServiceFee> serviceFees = new HashSet<>();

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TrainerTagRelation> trainerTagRelations = new HashSet<>();


    public void addCertification(Certification certification) {
        certifications.add(certification);
        certification.setTrainer(this);
    }

    public void addPhoto(TrainerPhoto photo) {
        photos.add(photo);
        photo.setTrainer(this); // Photo 엔티티에도 Trainer 설정
    }

    public void removePhoto(TrainerPhoto photo) {
        photos.remove(photo);
        photo.setTrainer(null); // Photo 엔티티에서 Trainer 연결 해제
    }

    public void addServiceFee(TrainerServiceFee fee) {
        serviceFees.add(fee);
        fee.setTrainer(this); // ServiceFee 엔티티에도 Trainer 설정
    }

    public void removeServiceFee(TrainerServiceFee fee) {
        serviceFees.remove(fee);
        fee.setTrainer(null); // ServiceFee 엔티티에서 Trainer 연결 해제
    }

}

