package org.lucky0111.pettalk.domain.entity.trainer;

import jakarta.persistence.*;
import lombok.*;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Builder
    protected Certification(Trainer trainer, String certName, String issuingBody, LocalDate issueDate, String fileUrl){
    this.trainer = trainer;
    this.certName = certName;
    this.issuingBody = issuingBody;
    this.issueDate = issueDate;
    this.fileUrl = fileUrl;

    this.approved = false;
    this.rejected = false;
    }

    // --- 비즈니스 로직을 포함한 상태 변경 메소드 ---

    protected void associateTrainer(Trainer trainer) {
        this.trainer = trainer;
    }
    public void approve() {
        if (this.rejected) {
            throw new IllegalStateException("이미 거절된 자격증은 승인할 수 없습니다.");
        }
        if (this.approved) {
            return;
        }
        this.approved = true;
        this.rejected = false;
    }

    public void reject() {
        if (this.approved) {
            throw new IllegalStateException("이미 승인된 자격증은 거절할 수 없습니다.");
        }
        if (this.rejected) {
            return;
        }
        this.rejected = true;
        this.approved = false;
    }

    public void updateFileUrl(String newFileUrl) {
        if (newFileUrl == null || newFileUrl.isBlank()) {
            throw new IllegalArgumentException("파일 URL은 비워둘 수 없습니다.");
        }
        this.fileUrl = newFileUrl;
    }
}
