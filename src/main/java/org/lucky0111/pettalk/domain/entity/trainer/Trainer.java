package org.lucky0111.pettalk.domain.entity.trainer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.entity.user.PetUser;

import java.time.LocalDateTime;
import java.util.UUID;

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

    private Integer experienceYears;
    private LocalDateTime approvedAt;

    @Column(length = 1000)
    private String representativeCareer; // 대표 경력
    private String specializationText; // 예: "행동 교정, 아질리티, 기본 복종"
    private String visitingAreas; // 예: "강남구, 서초구, 송파구, 분당"
    private String serviceFees; // 예: "시간당 5만원, 5회 패키지 20만원"
    @Column(length = 1000)
    private String introduction; // 자기소개
}

