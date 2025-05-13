package org.lucky0111.pettalk.domain.entity.community;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.lucky0111.pettalk.domain.entity.user.PetUser;

@Setter
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
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name = "FK_POST_LIKE",
            foreignKeyDefinition = "FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE"))
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private PetUser user;
}
