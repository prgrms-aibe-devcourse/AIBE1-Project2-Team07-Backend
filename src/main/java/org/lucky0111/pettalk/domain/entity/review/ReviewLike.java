package org.lucky0111.pettalk.domain.entity.review;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.lucky0111.pettalk.domain.entity.PetUser;

@Setter
@Getter
@Entity
@Table(name = "review_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"review_id", "user_id"})
},
        indexes = {
                @Index(name = "idx_review_like_review", columnList = "review_id"),
                @Index(name = "idx_review_like_user", columnList = "user_id")
        })
public class ReviewLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private PetUser user;
}
