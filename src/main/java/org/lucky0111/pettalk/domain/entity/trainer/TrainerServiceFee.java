package org.lucky0111.pettalk.domain.entity.trainer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.common.ServiceType;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "trainer_service_fees")
public class TrainerServiceFee extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Trainer와의 N:1 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    // 교육 종류 (방문 교육, 영상 교육 등)
    @Enumerated(EnumType.STRING) // Enum 상수의 이름을 문자열로 DB에 저장
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType; // <--- TrainerServiceFeeType Enum 사용

    // 교육 시간 (분 단위)
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes; // 교육 시간 (분)

    // 요금
    @Column(name = "fee_amount", nullable = false, precision = 10, scale = 2) // BigDecimal은 정밀도(precision)와 스케일(scale) 설정 필요
    private BigDecimal feeAmount; // 요금 (정확한 계산 위해 BigDecimal 사용)
}
