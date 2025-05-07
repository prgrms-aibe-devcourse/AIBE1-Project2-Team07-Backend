package org.lucky0111.pettalk.controller.community;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.dto.community.*;
import org.lucky0111.pettalk.service.community.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer token"
)
@Tag(name = "댓글 API", description = "댓글 관련 API 엔드포인트")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "게시물의 댓글 목록 조회", description = "특정 게시물의 댓글 목록을 조회합니다.")
    @GetMapping("/api/v1/posts/{postId}/comments")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CommentsResponseDTO> getCommentsByPostId(
            @PathVariable Long postId,
            @RequestParam(required = false) Long cursor) {
        log.info("게시물의 댓글 목록 조회 요청: postId={} cursor={}", postId, cursor);

        CommentsResponseDTO comments = commentService.getCommentsByPostId(postId, cursor);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "댓글의 대댓글 목록 조회", description = "특정 댓글의 대댓글 목록을 조회합니다.")
    @GetMapping("/api/v1/comments/{commentId}/replies")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<CommentResponseDTO>> getRepliesByCommentId(
            @PathVariable Long commentId,
            @RequestParam(required = false) Long cursor) {
        log.info("댓글의 대댓글 목록 조회 요청: commentId={} cursor={}", commentId, cursor);

        List<CommentResponseDTO> replies = commentService.getRepliesByCommentId(commentId, cursor);
        return ResponseEntity.ok(replies);
    }

    @Operation(summary = "댓글 작성", description = "게시물에 새 댓글을 작성합니다.")
    @PostMapping("/api/v1/posts/{postId}/comments")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CommentResponseDTO> createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequestDTO requestDTO) {
        log.info("댓글 작성 요청: postId={}, {}", postId, requestDTO);

        CommentResponseDTO createdComment = commentService.createComment(postId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @Operation(summary = "댓글 수정", description = "기존 댓글을 수정합니다.")
    @PutMapping("/api/v1/comments/{commentId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CommentResponseDTO> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateDTO updateDTO) {
        log.info("댓글 수정 요청: commentId={}, {}", commentId, updateDTO);

        CommentResponseDTO updatedComment = commentService.updateComment(commentId, updateDTO);
        return ResponseEntity.ok(updatedComment);
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/api/v1/comments/{commentId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId) {
        log.info("댓글 삭제 요청: commentId={}", commentId);

        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "작성한 댓글 목록 조회", description = "작성한 댓글 목록을 조회합니다.")
    @GetMapping("/api/v1/comments/users/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<MyCommentResponseDTO>> getaMyComments() {
        log.info("댓글 작성 목록 조회 요청");

        List<MyCommentResponseDTO> myComments = commentService.getMyComments();
        return ResponseEntity.ok(myComments);
    }
}