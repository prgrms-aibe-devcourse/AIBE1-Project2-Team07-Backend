package org.lucky0111.pettalk.domain.entity.trainer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;

import java.time.LocalDate;

@Getter
@Setter
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
    private Boolean approved = false;
    private Boolean rejected = false;

    public void updateApprovalStatus(Boolean approved, Boolean rejected) {
        this.approved = approved;
        this.rejected = rejected;
    }

}
