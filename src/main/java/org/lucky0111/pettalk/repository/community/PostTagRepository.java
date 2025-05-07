package org.lucky0111.pettalk.repository.community;

import org.lucky0111.pettalk.domain.entity.community.Post;
import org.lucky0111.pettalk.domain.entity.community.PostTagRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTagRelation, Long> {
    // 특정 게시물의 태그 목록 조회
    List<PostTagRelation> findByPost(Post post);

    // 특정 게시물의 태그 이름 목록 조회
    @Query("SELECT pt.tag.tagName as tagName " +
            "FROM PostTagRelation pt " +
            "WHERE pt.post.postId = :postId")
    List<PostTagProjection> findTagNamesByPostId(@Param("postId") Long postId);

    // 게시물 목록에 대한 태그 이름 목록 일괄 조회
    @Query("SELECT pt.post.postId as postId, pt.tag.tagName as tagName " +
            "FROM PostTagRelation pt " +
            "WHERE pt.post.postId IN :postIds")
    List<PostTagProjection> findTagNamesByPostIds(@Param("postIds") List<Long> postIds);

    // 게시물 태그를 위한 프로젝션 인터페이스
    interface PostTagProjection {
        Long getPostId();
        String getTagName();
    }

    // 게시물별 태그 삭제
    void deleteByPost(Post post);
}
