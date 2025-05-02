package org.lucky0111.pettalk.domain.entity.community;

import jakarta.persistence.*;
import lombok.Getter;
import org.lucky0111.pettalk.domain.entity.PetUser;

@Getter
@Entity
@Table(name = "post_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_id", "user_id"})
})
public class PostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private PetUser user;
}
