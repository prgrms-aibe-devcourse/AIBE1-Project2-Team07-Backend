package org.lucky0111.pettalk.domain.entity.community;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.entity.user.PetUser;

@Setter
@Getter
@Entity
@Table(name = "comments")
@NoArgsConstructor
public class Comment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne
    @JoinColumn(name = "post_id",
            foreignKey = @ForeignKey(name = "FK_CHILD_PARENT",
                    foreignKeyDefinition = "FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE"))
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private PetUser user;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    private String content;
}
