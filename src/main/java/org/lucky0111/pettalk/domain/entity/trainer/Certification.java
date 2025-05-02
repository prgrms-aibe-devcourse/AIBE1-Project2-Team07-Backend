package org.lucky0111.pettalk.domain.entity.trainer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "certifications")
public class Certification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long certId;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    private String certName;
    private String issuingBody;
    private LocalDate issueDate;
    private String fileUrl;
    private Boolean approved;

}
