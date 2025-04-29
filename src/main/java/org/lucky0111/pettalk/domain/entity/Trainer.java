package org.lucky0111.pettalk.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "trainers")
@NoArgsConstructor
public class Trainer extends BaseTimeEntity {
    @Id
    private String trainerId; // FK -> PetUser.userId

    @OneToOne
    @JoinColumn(name = "trainer_id")
    private PetUser user;

    private String introduction;
    private Integer experienceYears;

    private LocalDateTime approvedAt;
}

