package org.lucky0111.pettalk.repository.community;

import org.lucky0111.pettalk.domain.entity.community.Post;
import org.lucky0111.pettalk.domain.entity.community.PostTagRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTagRelation, Long> {
    @Query("SELECT pt.tag.tagName as tagName " +
            "FROM PostTagRelation pt " +
            "WHERE pt.post.postId = :postId")
    List<PostTagProjection> findTagNamesByPostId(@Param("postId") Long postId);

    @Query("SELECT pt.post.postId as postId, pt.tag.tagName as tagName " +
            "FROM PostTagRelation pt " +
            "WHERE pt.post.postId IN :postIds")
    List<PostTagProjection> findTagNamesByPostIds(@Param("postIds") List<Long> postIds);

    interface PostTagProjection {
        Long getPostId();
        String getTagName();
    }

    void deleteByPost(Post post);
}
