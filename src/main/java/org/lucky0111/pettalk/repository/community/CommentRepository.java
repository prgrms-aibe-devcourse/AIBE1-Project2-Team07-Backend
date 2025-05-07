package org.lucky0111.pettalk.repository.community;

import org.lucky0111.pettalk.domain.entity.community.Comment;
import org.lucky0111.pettalk.domain.entity.community.Post;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    int countByPost(Post post);

    List<Comment> findByParentComment(Comment parentComment);

    List<Comment> findByPostAndParentCommentIsNull(Post post, PageRequest pageRequest);

    List<Comment> findByPostAndParentCommentIsNullAndCommentIdGreaterThan(Post post, Long commentId, PageRequest pageRequest);

    @Query("SELECT p.postId as postId, COUNT(c) as commentCount FROM Post p " +
            "LEFT JOIN Comment c ON c.post = p " +
            "WHERE p.postId IN :postIds " +
            "GROUP BY p.postId")
    List<CommentCountProjection> countCommentsByPostIds(@Param("postIds") List<Long> postIds);

    interface CommentCountProjection {
        Long getPostId();
        Integer getCommentCount();
    }

    void deleteByPost(Post post);

    int countByParentComment(Comment parentComment);

    List<Comment> findTop3ByParentCommentOrderByCreatedAtAsc(Comment parentComment);

    @Query("SELECT c FROM Comment c WHERE c.parentComment = :parentComment AND c.commentId NOT IN :previewIds ORDER BY c.createdAt ASC")
    List<Comment> findRemainingRepliesByParentComment(
            @Param("parentComment") Comment parentComment,
            @Param("previewIds") List<Long> previewIds,
            Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.parentComment = :parentComment " +
            "AND c.commentId NOT IN :previewIds " +
            "AND c.commentId < :cursor " +
            "ORDER BY c.commentId DESC")
    List<Comment> findRemainingRepliesWithCursor(
            @Param("parentComment") Comment parentComment,
            @Param("previewIds") List<Long> previewIds,
            @Param("cursor") Long cursor,
            Pageable pageable);
}