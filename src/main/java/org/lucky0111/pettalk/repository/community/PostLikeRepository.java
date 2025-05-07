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
    // 게시물별 좋아요 수 조회
    int countByPost(Post post);

    // 특정 사용자의 좋아요 여부 조회
    boolean existsByPostAndUser(Post post, PetUser user);
    boolean existsByPostAndUser_UserId(Post post, UUID userId);

    // 특정 게시물 & 사용자의 좋아요 조회
    Optional<PostLike> findByPostAndUser(Post post, PetUser user);

    // 게시물 목록에 대한 좋아요 수 일괄 조회
    @Query("SELECT p.postId as postId, COUNT(pl.likeId) as likeCount " +
            "FROM Post p " +
            "LEFT JOIN PostLike pl ON pl.post = p " +
            "WHERE p.postId IN :postIds " +
            "GROUP BY p.postId")
    List<PostLikeCountProjection> countLikesByPostIds(@Param("postIds") List<Long> postIds);

    // 게시물 목록에 대한 사용자 좋아요 여부 일괄 조회
    @Query("SELECT p.postId as postId, " +
            "CASE WHEN COUNT(pl.likeId) > 0 THEN true ELSE false END as hasLiked " +
            "FROM Post p " +
            "LEFT JOIN PostLike pl ON pl.post = p AND pl.user.userId = :userId " +
            "WHERE p.postId IN :postIds " +
            "GROUP BY p.postId")
    List<PostLikeStatusProjection> checkUserLikeStatus(
            @Param("postIds") List<Long> postIds,
            @Param("userId") UUID userId);

    // 게시물별 좋아요 수를 위한 프로젝션 인터페이스
    interface PostLikeCountProjection {
        Long getPostId();
        Integer getLikeCount();
    }

    // 게시물별 사용자 좋아요 여부를 위한 프로젝션 인터페이스
    interface PostLikeStatusProjection {
        Long getPostId();
        Boolean getHasLiked();
    }

    // 게시물별 좋아요 삭제
    void deleteByPost(Post post);
}
