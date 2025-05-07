package org.lucky0111.pettalk.service.community;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.common.ErrorCode;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.dto.community.CommentRequestDTO;
import org.lucky0111.pettalk.domain.dto.community.CommentResponseDTO;
import org.lucky0111.pettalk.domain.dto.community.CommentUpdateDTO;
import org.lucky0111.pettalk.domain.dto.community.CommentsResponseDTO;
import org.lucky0111.pettalk.domain.entity.community.Comment;
import org.lucky0111.pettalk.domain.entity.community.Post;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.exception.CustomException;
import org.lucky0111.pettalk.repository.community.CommentRepository;
import org.lucky0111.pettalk.repository.community.PostRepository;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.lucky0111.pettalk.util.error.ExceptionUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int COMMENT_LIMIT = 10;

    @Override
    @Transactional
    public CommentResponseDTO createComment(CommentRequestDTO requestDTO) {
        PetUser currentUser = getCurrentUser();
        Post post = findPostById(requestDTO.postId());
        Comment parentComment = findParentCommentIfExists(requestDTO.parentCommentId());

        Comment comment = buildComment(post, currentUser, parentComment, requestDTO.content());
        Comment savedComment = commentRepository.save(comment);

        return convertToResponseDTO(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentsResponseDTO getCommentsByPostId(Long postId, Long cursor) {
        Post post = findPostById(postId);
        List<Comment> rootComments = fetchRootComments(post, cursor);

        boolean hasMore = hasMoreComments(rootComments);
        Long nextCursor = calculateNextCursor(rootComments, hasMore);
        List<CommentResponseDTO> commentDTOs = buildCommentResponsesWithReplies(rootComments);

        return new CommentsResponseDTO(commentDTOs, nextCursor, hasMore);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getRepliesByCommentId(Long commentId, Long cursor) {
        Comment parentComment = findCommentById(commentId);
        List<Long> previewIds = getPreviewReplyIds(parentComment);
        List<Comment> remainingReplies = fetchRemainingReplies(parentComment, previewIds, cursor);

        return convertCommentsToResponseDTOs(remainingReplies);
    }

    @Override
    @Transactional
    public CommentResponseDTO updateComment(Long commentId, CommentUpdateDTO updateDTO) {
        UUID currentUserUUID = getCurrentUserUUID();
        Comment comment = findCommentById(commentId);

        validateCommentOwnership(comment, currentUserUUID);
        comment.setContent(updateDTO.content());

        Comment updatedComment = commentRepository.save(comment);
        return convertToResponseDTO(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        UUID currentUserUUID = getCurrentUserUUID();
        Comment comment = findCommentById(commentId);

        validateCommentOwnership(comment, currentUserUUID);
        handleCommentDeletion(comment);
    }

    // 게시물 조회 관련 메소드
    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("해당 게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    // 댓글 조회 관련 메소드
    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException("해당 댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    // 부모 댓글 조회 메소드
    private Comment findParentCommentIfExists(Long parentCommentId) {
        if (parentCommentId == null) {
            return null;
        }

        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new CustomException("해당 부모 댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (parentComment.getParentComment() != null) {
            throw new IllegalArgumentException("답글에는 답글을 달 수 없습니다.");
        }

        return parentComment;
    }

    // 댓글 생성 관련 메소드
    private Comment buildComment(Post post, PetUser user, Comment parentComment, String content) {
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setParentComment(parentComment);
        comment.setContent(content);
        return comment;
    }

    private List<Comment> fetchRootComments(Post post, Long cursor) {
        if (cursor == null) {
            return commentRepository.findByPostAndParentCommentIsNull(
                    post, PageRequest.of(0, COMMENT_LIMIT));
        } else {
            return commentRepository.findByPostAndParentCommentIsNullAndCommentIdGreaterThan(
                    post, cursor, PageRequest.of(0, COMMENT_LIMIT));
        }
    }

    // 댓글 더 있는지 확인 메소드
    private boolean hasMoreComments(List<Comment> comments) {
        return comments.size() == COMMENT_LIMIT;
    }

    private Long calculateNextCursor(List<Comment> comments, boolean hasMore) {
        if (!comments.isEmpty() && hasMore) {
            return comments.get(comments.size() - 1).getCommentId();
        }
        return null;
    }

    // 미리보기 답글 ID 목록 가져오기
    private List<Long> getPreviewReplyIds(Comment parentComment) {
        List<Comment> previewReplies = commentRepository.findTop3ByParentCommentOrderByCreatedAtAsc(parentComment);
        return previewReplies.stream()
                .map(Comment::getCommentId)
                .collect(Collectors.toList());
    }

    // 나머지 답글 가져오기
    private List<Comment> fetchRemainingReplies(Comment parentComment, List<Long> previewIds, Long cursor) {
        if (cursor == null) {
            return commentRepository.findRemainingRepliesByParentComment(
                    parentComment, previewIds, PageRequest.of(0, COMMENT_LIMIT));
        } else {
            return commentRepository.findRemainingRepliesWithCursor(
                    parentComment, previewIds, cursor, PageRequest.of(0, COMMENT_LIMIT));
        }
    }

    // 댓글 목록을 ResponseDTO 목록으로 변환
    private List<CommentResponseDTO> convertCommentsToResponseDTOs(List<Comment> comments) {
        return comments.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // 루트 댓글 목록에 답글 정보를 추가하여 ResponseDTO 목록 생성
    private List<CommentResponseDTO> buildCommentResponsesWithReplies(List<Comment> rootComments) {
        return rootComments.stream()
                .map(this::buildCommentWithReplies)
                .collect(Collectors.toList());
    }

    // 단일 댓글에 답글 정보 추가하여 ResponseDTO 생성
    private CommentResponseDTO buildCommentWithReplies(Comment comment) {
        List<Comment> previewReplies = commentRepository.findTop3ByParentCommentOrderByCreatedAtAsc(comment);
        List<CommentResponseDTO> replyDtos = convertCommentsToResponseDTOs(previewReplies);
        int replyCount = commentRepository.countByParentComment(comment);

        CommentResponseDTO dto = convertToResponseDTO(comment);

        return new CommentResponseDTO(
                dto.commentId(),
                dto.postId(),
                dto.userName(),
                dto.userNickname(),
                dto.profileImageUrl(),
                dto.parentCommentId(),
                dto.content(),
                replyDtos,
                replyCount,
                dto.createdAt(),
                dto.updatedAt()
        );
    }

    // 댓글 소유권 검증
    private void validateCommentOwnership(Comment comment, UUID userUUID) {
        if (!comment.getUser().getUserId().equals(userUUID)) {
            throw new CustomException("댓글에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
    }

    // 댓글 삭제 처리
    private void handleCommentDeletion(Comment comment) {
        List<Comment> replies = commentRepository.findByParentComment(comment);

        if (!replies.isEmpty()) {
            // 답글이 있으면 소프트 삭제
            comment.setContent("삭제된 댓글입니다.");
            commentRepository.save(comment);
        } else {
            // 답글이 없으면 완전히 삭제
            commentRepository.delete(comment);
        }
    }

    // 댓글을 ResponseDTO로 변환
    private CommentResponseDTO convertToResponseDTO(Comment comment) {
        String createdAt = formatDateTime(comment.getCreatedAt());
        String updatedAt = formatDateTime(comment.getUpdatedAt());
        Long parentCommentId = getParentCommentId(comment);

        return new CommentResponseDTO(
                comment.getCommentId(),
                comment.getPost().getPostId(),
                comment.getUser().getName(),
                comment.getUser().getNickname(),
                comment.getUser().getProfileImageUrl(),
                parentCommentId,
                comment.getContent(),
                new ArrayList<>(),
                commentRepository.countByParentComment(comment),
                createdAt,
                updatedAt
        );
    }

    // 날짜 시간 포맷팅
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : null;
    }

    // 부모 댓글 ID 가져오기
    private Long getParentCommentId(Comment comment) {
        return comment.getParentComment() != null ?
                comment.getParentComment().getCommentId() : null;
    }

    // 현재 사용자 UUID 가져오기
    private UUID getCurrentUserUUID() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomOAuth2User userDetails) {
            return userDetails.getUserId();
        }

        throw ExceptionUtils.of(ErrorCode.UNAUTHORIZED);
    }

    // 현재 사용자 정보 가져오기
    private PetUser getCurrentUser() {
        UUID currentUserUUID = getCurrentUserUUID();
        return petUserRepository.findById(currentUserUUID)
                .orElseThrow(() -> ExceptionUtils.of(ErrorCode.USER_NOT_FOUND));
    }
}
