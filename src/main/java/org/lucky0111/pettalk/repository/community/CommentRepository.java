package org.lucky0111.pettalk.repository.community;

import org.lucky0111.pettalk.domain.entity.community.Comment;
import org.lucky0111.pettalk.domain.entity.community.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 게시물별 댓글 수 조회
    int countByPost(Post post);

    // 게시물별 최상위 댓글 조회 (부모 댓글이 없는 댓글)
    List<Comment> findByPostAndParentCommentIsNull(Post post);

    // 특정 댓글에 대한 답글 조회
    List<Comment> findByParentComment(Comment parentComment);

    // 특정 게시물별 모든 댓글 조회
    List<Comment> findByPost(Post post);

    // 게시물 목록에 대한 댓글 수 일괄 조회
    @Query("SELECT p.postId as postId, COUNT(c) as commentCount FROM Post p " +
            "LEFT JOIN Comment c ON c.post = p " +
            "WHERE p.postId IN :postIds " +
            "GROUP BY p.postId")
    List<CommentCountProjection> countCommentsByPostIds(@Param("postIds") List<Long> postIds);

    // 게시물별 댓글 수를 위한 프로젝션 인터페이스
    interface CommentCountProjection {
        Long getPostId();
        Integer getCommentCount();
    }

    // 게시물별 댓글 삭제
    void deleteByPost(Post post);
}