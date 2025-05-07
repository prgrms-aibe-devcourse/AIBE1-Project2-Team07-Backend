package org.lucky0111.pettalk.service.community;

import jakarta.servlet.http.HttpServletRequest;
import org.lucky0111.pettalk.domain.dto.community.*;

import java.util.List;

public interface CommentService {
    CommentResponseDTO createComment(Long postId, CommentRequestDTO requestDTO);
    CommentsResponseDTO getCommentsByPostId(Long postId, Long cursor);
    CommentResponseDTO updateComment(Long commentId, CommentUpdateDTO updateDTO);
    void deleteComment(Long commentId);

    List<CommentResponseDTO> getRepliesByCommentId(Long commentId, Long cursor);

    List<MyCommentResponseDTO> getMyComments();
}