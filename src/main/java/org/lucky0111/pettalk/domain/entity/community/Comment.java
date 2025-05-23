package org.lucky0111.pettalk.domain.entity.community;

import jakarta.persistence.*;
import lombok.*;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.dto.community.CommentRequestDTO;
import org.lucky0111.pettalk.domain.entity.user.PetUser;

@Getter
@Entity
@Table(name = "comments")
@NoArgsConstructor
public class Comment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id",
            foreignKey = @ForeignKey(name = "FK_CHILD_PARENT",
                    foreignKeyDefinition = "FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE"),
    nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private PetUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Column(nullable = false)
    private String content;

    @Builder
    public Comment(Post post, PetUser user, Comment parentComment, String content) {
        this.post = post;
        this.user = user;
        this.parentComment = parentComment;
        this.content = content;
    }

    public static Comment from(@NonNull Post post,@NonNull PetUser user, Comment parentComment,@NonNull String content) {
        return Comment.builder()
                .post(post)
                .user(user)
                .parentComment(parentComment)
                .content(content)
                .build();
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
