package org.lucky0111.pettalk.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.entity.match.UserApply;

@Getter
@Entity
@Table(name = "reviews")
@NoArgsConstructor
public class Review extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @OneToOne
    @JoinColumn(name = "apply_id")
    private UserApply userApply;

    private Integer rating;

    private String reviewImageUrl;
    private String title;
    private String comment;
}
