package org.lucky0111.pettalk.repository.community;

import org.lucky0111.pettalk.domain.entity.community.Post;
import org.lucky0111.pettalk.domain.entity.community.PostLike;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByPostAndUser_UserId(Post post, UUID userId);

    Optional<PostLike> findByPostAndUser(Post post, PetUser user);

    @Query("SELECT p.postId as postId, " +
            "CASE WHEN COUNT(pl.likeId) > 0 THEN true ELSE false END as hasLiked " +
            "FROM Post p " +
            "LEFT JOIN PostLike pl ON pl.post = p AND pl.user.userId = :userId " +
            "WHERE p.postId IN :postIds " +
            "GROUP BY p.postId")
    List<PostLikeStatusProjection> checkUserLikeStatus(
            @Param("postIds") List<Long> postIds,
            @Param("userId") UUID userId);

    @Query("SELECT pl.post FROM PostLike pl WHERE pl.user.userId = :userId")
    List<Post> findPostsByUserId(UUID userId);

    void deleteByPost(Post post);

    interface PostLikeCountProjection {
        Long getPostId();
        Integer getLikeCount();
    }

    interface PostLikeStatusProjection {
        Long getPostId();
        Boolean getHasLiked();
    }
}
