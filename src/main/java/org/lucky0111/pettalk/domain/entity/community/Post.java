package org.lucky0111.pettalk.domain.entity.community;

import jakarta.persistence.*;
import lombok.*;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.common.PetCategory;
import org.lucky0111.pettalk.domain.dto.community.PostRequestDTO;
import org.lucky0111.pettalk.domain.dto.community.PostUpdateDTO;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.domain.common.PostCategory;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_post_title", columnList = "title"),
        @Index(name = "idx_post_category", columnList = "postCategory"),
        @Index(name = "idx_pet_category", columnList = "petCategory"),
        @Index(name = "idx_like_count", columnList = "likeCount"),
        @Index(name = "idx_comment_count", columnList = "commentCount")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private PetUser user;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
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

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    @Builder
    public Post(@NonNull PetUser user, @NonNull PetCategory petCategory, @NonNull PostCategory postCategory, @NonNull String title, @NonNull String content) {
        this.user = user;
        this.petCategory = petCategory;
        this.postCategory = postCategory;
        this.title = title;
        this.content = content;
        this.commentCount = 0;
        this.likeCount = 0;
    }

    public static Post of(PostRequestDTO requestDTO, PetUser user) {
        return Post.builder()
                .user(user)
                .petCategory(requestDTO.petCategory())
                .postCategory(requestDTO.postCategory())
                .title(requestDTO.title())
                .content(requestDTO.content())
                .build();
    }

    public void updateVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void updatePost(PostUpdateDTO updateDTO) {
        if (updateDTO.postCategory() != null) {
            this.postCategory = updateDTO.postCategory();
        }
        if (updateDTO.petCategory() != null) {
            this.petCategory = updateDTO.petCategory();
        }
        if (updateDTO.title() != null && !updateDTO.title().trim().isEmpty()) {
            this.title = updateDTO.title().trim();
        }
        if (updateDTO.content() != null && !updateDTO.content().trim().isEmpty()) {
            this.content = updateDTO.content().trim();
        }
    }
}

