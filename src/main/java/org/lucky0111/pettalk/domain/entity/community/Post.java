package org.lucky0111.pettalk.domain.entity.community;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.common.PetCategory;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.domain.common.PostCategory;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_post_title", columnList = "title"),
        @Index(name = "idx_post_category", columnList = "postCategory"),
        @Index(name = "idx_pet_category", columnList = "petCategory"),
        @Index(name = "idx_like_count", columnList = "likeCount"),
        @Index(name = "idx_comment_count", columnList = "commentCount")
})
@NoArgsConstructor
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PostCategory postCategory;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PetCategory petCategory;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private PetUser user;

    private String title;

    @Lob
    @Column
    private String content;

    private String videoUrl;

    @Column(nullable = false)
    private Integer commentCount = 0;

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    @Column(nullable = false)
    private Integer likeCount = 0;

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementCLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    public void addImage(PostImage image) {
        this.images.add(image);
        image.setPost(this);
    }

    public void removeImage(PostImage image) {
        this.images.remove(image);
        image.setPost(null);
    }
}
