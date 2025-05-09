package org.lucky0111.pettalk.domain.entity.match;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.common.ApplyStatus;
import org.lucky0111.pettalk.domain.common.ServiceType;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.domain.entity.trainer.Trainer;

@Setter
@Getter
@Entity
@Table(name = "user_applies", indexes = {
        @Index(name = "idx_user_apply_user", columnList = "user_id"),
        @Index(name = "idx_user_apply_trainer", columnList = "trainer_id"),
        @Index(name = "idx_user_apply_status", columnList = "applyStatus"),
        @Index(name = "idx_user_trainer_status", columnList = "user_id, trainer_id, applyStatus")
})
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
}
