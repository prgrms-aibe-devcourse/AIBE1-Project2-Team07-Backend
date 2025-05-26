package org.lucky0111.pettalk.domain.entity.match;

import jakarta.persistence.*;
import lombok.*;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.common.ApplyReason;

@Getter
@Entity
@Table(name = "apply_answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplyAnswer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long responseId;

    @Enumerated(EnumType.STRING)
    private ApplyReason applyReason;

    @OneToOne
    @JoinColumn(name = "apply_id", foreignKey = @ForeignKey(name = "FK_APPLY_ANSWER",
            foreignKeyDefinition = "FOREIGN KEY (apply_id) REFERENCES user_applies(apply_id) ON DELETE CASCADE"))
    private UserApply userApply;

    @Column(length = 500, nullable = false)
    private String content;

    @Builder
    public ApplyAnswer(ApplyReason applyReason, UserApply userApply, String content) {
        this.applyReason = applyReason;
        this.userApply = userApply;
        this.content = content;
    }

    public static ApplyAnswer from(@NonNull ApplyReason applyReason, @NonNull UserApply userApply, @NonNull String content) {
        return ApplyAnswer.builder()
                .applyReason(applyReason)
                .userApply(userApply)
                .content(content)
                .build();
    }

    public void updateContentAndReason(@NonNull String content, @NonNull ApplyReason reason) {
        this.content = content;
        this.applyReason = reason;
    }
}
