package org.lucky0111.pettalk.domain.entity.match;

import jakarta.persistence.*;
import lombok.*;
import org.lucky0111.pettalk.domain.common.ApplyStatus;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.common.ServiceType;
import org.lucky0111.pettalk.domain.entity.trainer.Trainer;
import org.lucky0111.pettalk.domain.entity.user.PetUser;

@Getter
@Entity
@Table(name = "user_applies", indexes = {
        @Index(name = "idx_user_apply_user", columnList = "user_id"),
        @Index(name = "idx_user_apply_trainer", columnList = "trainer_id"),
        @Index(name = "idx_user_apply_status", columnList = "applyStatus"),
        @Index(name = "idx_user_trainer_status", columnList = "user_id, trainer_id, applyStatus")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserApply extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applyId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private PetUser petUser;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    @Column(length = 100, nullable = false)
    private String petType;

    @Column(length = 100, nullable = false)
    private String petBreed;

    @Column(nullable = false)
    private Integer petMonthAge;

    @Column(length = 500, nullable = false)
    private String content;
    private String imageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplyStatus applyStatus;

    @Column(nullable = false)
    private boolean hasReviewed = false;

    @Builder
    public UserApply(PetUser petUser,
                     Trainer trainer,
                     ServiceType serviceType,
                     String petType,
                     String petBreed,
                     Integer petMonthAge,
                     String content,
                     ApplyStatus applyStatus) {
        this.petUser = petUser;
        this.trainer = trainer;
        this.serviceType = serviceType;
        this.petType = petType;
        this.petBreed = petBreed;
        this.petMonthAge = petMonthAge;
        this.content = content;
        this.applyStatus = applyStatus;
    }

    public void updateApplyStatus(@NonNull ApplyStatus status) {
        this.applyStatus = status;
    }

    public void updateReviewedStatus(boolean hasReviewed) {
        this.hasReviewed = hasReviewed;
    }
}
