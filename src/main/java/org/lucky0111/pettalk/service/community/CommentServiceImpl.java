package org.lucky0111.pettalk.service.community;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.dto.community.CommentRequestDTO;
import org.lucky0111.pettalk.domain.dto.community.CommentResponseDTO;
import org.lucky0111.pettalk.domain.dto.community.CommentUpdateDTO;
import org.lucky0111.pettalk.domain.entity.community.Comment;
import org.lucky0111.pettalk.domain.entity.community.Post;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.exception.CustomException;
import org.lucky0111.pettalk.repository.community.CommentRepository;
import org.lucky0111.pettalk.repository.community.PostRepository;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.lucky0111.pettalk.util.auth.JWTUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final PetUserRepository petUserRepository;
    private final JWTUtil jwtUtil;

    @Override
    @Transactional
    public CommentResponseDTO createComment(CommentRequestDTO requestDTO, HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);
        PetUser currentUser = getCurrentUser(request);

        Post post = postRepository.findById(requestDTO.postId())
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물을 찾을 수 없습니다."));

        Comment parentComment = null;
        if (requestDTO.parentCommentId() != null) {
            parentComment = commentRepository.findById(requestDTO.parentCommentId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 부모 댓글을 찾을 수 없습니다."));

            // 부모 댓글이 이미 다른 댓글의 답글인 경우 (대댓글의 대댓글 방지)
            if (parentComment.getParentComment() != null) {
                throw new IllegalArgumentException("답글에는 답글을 달 수 없습니다.");
            }
        }

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(currentUser);
        comment.setParentComment(parentComment);
        comment.setContent(requestDTO.content());

        Comment savedComment = commentRepository.save(comment);

        return convertToResponseDTO(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByPostId(Long postId, HttpServletRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물을 찾을 수 없습니다."));

        // 루트 댓글만 조회 (부모 댓글이 없는 댓글)
        List<Comment> rootComments = commentRepository.findByPostAndParentCommentIsNull(post);

        // 계층 구조로 변환
        return rootComments.stream()
                .map(this::convertToResponseDTOWithReplies)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponseDTO updateComment(Long commentId, CommentUpdateDTO updateDTO, HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 댓글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!comment.getUser().getUserId().equals(currentUserUUID)) {
            throw new CustomException("댓글을 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        // 내용 업데이트
        comment.setContent(updateDTO.content());

        Comment updatedComment = commentRepository.save(comment);

        return convertToResponseDTO(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 댓글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!comment.getUser().getUserId().equals(currentUserUUID)) {
            throw new CustomException("댓글을 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        // 답글이 있는 경우 내용만 삭제 처리 (soft delete)
        List<Comment> replies = commentRepository.findByParentComment(comment);
        if (!replies.isEmpty()) {
            comment.setContent("삭제된 댓글입니다.");
            commentRepository.save(comment);
        } else {
            // 답글이 없는 경우 완전히 삭제
            commentRepository.delete(comment);
        }
    }

    // 댓글을 ResponseDTO로 변환
    private CommentResponseDTO convertToResponseDTO(Comment comment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String createdAt = comment.getCreatedAt() != null ?
                comment.getCreatedAt().format(formatter) : null;

        String updatedAt = comment.getUpdatedAt() != null ?
                comment.getUpdatedAt().format(formatter) : null;

        Long parentCommentId = comment.getParentComment() != null ?
                comment.getParentComment().getCommentId() : null;

        return new CommentResponseDTO(
                comment.getCommentId(),
                comment.getPost().getPostId(),
                comment.getUser().getUserId(),
                comment.getUser().getName(),
                comment.getUser().getNickname(),
                comment.getUser().getProfileImageUrl(),
                parentCommentId,
                comment.getContent(),
                new ArrayList<>(), // 답글 목록 (별도 메서드에서 처리)
                createdAt,
                updatedAt
        );
    }

    // 댓글을 ResponseDTO로 변환 (답글 포함)
    private CommentResponseDTO convertToResponseDTOWithReplies(Comment comment) {
        // 답글 목록 조회
        List<Comment> replies = commentRepository.findByParentComment(comment);

        // 기본 DTO 생성
        CommentResponseDTO dto = convertToResponseDTO(comment);

        // 답글 목록 DTO 생성 및 설정
        List<CommentResponseDTO> replyDtos = replies.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        // 불변 리스트를 변경 가능한 리스트로 복사하여 설정
        return new CommentResponseDTO(
                dto.commentId(),
                dto.postId(),
                dto.userId(),
                dto.userName(),
                dto.userNickname(),
                dto.profileImageUrl(),
                dto.parentCommentId(),
                dto.content(),
                replyDtos,
                dto.createdAt(),
                dto.updatedAt()
        );
    }

    // JWT 토큰에서 현재 사용자 UUID 추출
    private UUID getCurrentUserUUID(HttpServletRequest request) {
        String token = extractJwtToken(request);
        if (token == null) {
            throw new RuntimeException("인증 토큰을 찾을 수 없습니다.");
        }
        return jwtUtil.getUserId(token);
    }

    // 현재 사용자 엔티티 조회
    private PetUser getCurrentUser(HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);
        return petUserRepository.findById(currentUserUUID)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }

    // 요청 헤더에서 JWT 토큰 추출
    private String extractJwtToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
