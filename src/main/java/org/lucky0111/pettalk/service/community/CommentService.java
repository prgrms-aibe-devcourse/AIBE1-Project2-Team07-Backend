package org.lucky0111.pettalk.service.community;

import jakarta.servlet.http.HttpServletRequest;
import org.lucky0111.pettalk.domain.dto.community.CommentRequestDTO;
import org.lucky0111.pettalk.domain.dto.community.CommentResponseDTO;
import org.lucky0111.pettalk.domain.dto.community.CommentUpdateDTO;

import java.util.List;

public interface CommentService {
    // 댓글 작성
    CommentResponseDTO createComment(CommentRequestDTO requestDTO, HttpServletRequest request);

    // 게시물의 댓글 목록 조회
    List<CommentResponseDTO> getCommentsByPostId(Long postId, HttpServletRequest request);

    // 댓글 수정
    CommentResponseDTO updateComment(Long commentId, CommentUpdateDTO updateDTO, HttpServletRequest request);

    // 댓글 삭제
    void deleteComment(Long commentId, HttpServletRequest request);
}