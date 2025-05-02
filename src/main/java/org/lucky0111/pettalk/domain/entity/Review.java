package org.lucky0111.pettalk.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "reviews")
@NoArgsConstructor
public class Review extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    // UserApply와의 다대일(@ManyToOne) 관계 매핑
    // reviews.apply_id (bigint FK) -> user_applies.apply_id (bigint PK)
    @ManyToOne(fetch = FetchType.LAZY) // 성능을 위해 지연 로딩(LAZY) 권장
    @JoinColumn(name = "apply_id", nullable = false) // reviews 테이블의 외래 키 컬럼명은 'apply_id'이며, 필수 값(null 불허)임
    private UserApply userApply; // UserApply 엔티티를 참조하는 필드

    private String chatroomId;

    private Integer rating;

    private String reviewImageUrl;
    private String title;
    private String comment;

}
