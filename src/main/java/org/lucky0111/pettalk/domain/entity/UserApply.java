package org.lucky0111.pettalk.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.entity.PetUser;
import org.lucky0111.pettalk.domain.entity.Trainer;
import org.lucky0111.pettalk.domain.common.Status;

@Data
@Entity
@Table(name = "user_applies")
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

    @Column(length = 500, nullable = false)
    private String content;

    private String imageUrl;

    private String videoUrl;

    @Column(nullable = false)
    private Status status;
}
