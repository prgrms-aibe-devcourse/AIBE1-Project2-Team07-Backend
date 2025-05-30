package org.lucky0111.pettalk.domain.entity.match;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.common.ApplyReason;

@Setter
@Getter
@Entity
@Table(name = "apply_answers")
@NoArgsConstructor
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

}
