package org.lucky0111.pettalk.service.community;

import jakarta.servlet.http.HttpServletRequest;
import org.lucky0111.pettalk.domain.dto.community.CommentRequestDTO;
import org.lucky0111.pettalk.domain.dto.community.CommentResponseDTO;
import org.lucky0111.pettalk.domain.dto.community.CommentUpdateDTO;
import org.lucky0111.pettalk.domain.dto.community.CommentsResponseDTO;

import java.util.List;

public interface CommentService {

    CommentResponseDTO createComment(CommentRequestDTO requestDTO);
    CommentsResponseDTO getCommentsByPostId(Long postId, Long cursor);
    CommentResponseDTO updateComment(Long commentId, CommentUpdateDTO updateDTO);
    void deleteComment(Long commentId);

    List<CommentResponseDTO> getRepliesByCommentId(Long commentId, Long cursor);
}