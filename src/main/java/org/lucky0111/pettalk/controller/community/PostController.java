package org.lucky0111.pettalk.controller.community;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.common.PetCategory;
import org.lucky0111.pettalk.domain.common.PostCategory;
import org.lucky0111.pettalk.domain.dto.community.PostLikeResponseDTO;
import org.lucky0111.pettalk.domain.dto.community.PostRequestDTO;
import org.lucky0111.pettalk.domain.dto.community.PostResponseDTO;
import org.lucky0111.pettalk.domain.dto.community.PostUpdateDTO;
import org.lucky0111.pettalk.service.community.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer token"
)
public class PostController {

    private final PostService postService;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "게시물 목록 조회", description = "게시물 목록을 페이지 단위로 조회합니다.")
    @GetMapping
    public ResponseEntity<List<PostResponseDTO>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) PostCategory postCategory,
            @RequestParam(required = false) PetCategory petCategory,
            HttpServletRequest request) {
        log.info("게시물 목록 조회 요청: page={}, postCategoryId={}, petCategoryId={}",
                page, postCategory, petCategory);

        List<PostResponseDTO> posts = postService.getAllPosts(page, postCategory, petCategory);
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "게시물 상세 조회", description = "특정 게시물을 상세 조회합니다.")
    @GetMapping("/{postId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PostResponseDTO> getPostById(
            @PathVariable Long postId) {
        log.info("게시물 상세 조회 요청: postId={}", postId);

        PostResponseDTO post = postService.getPostById(postId);
        return ResponseEntity.ok(post);
    }

    @Operation(summary = "게시물 작성", description = "새로운 게시물을 작성합니다.")
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PostResponseDTO> createPost(
            @RequestBody PostRequestDTO requestDTO) {
        log.info("게시물 작성 요청: {}", requestDTO);

        PostResponseDTO createdPost = postService.createPost(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @Operation(summary = "게시물 수정", description = "기존 게시물을 수정합니다.")
    @PutMapping("/{postId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable Long postId,
            @RequestBody PostUpdateDTO updateDTO) {
        log.info("게시물 수정 요청: postId={}, {}", postId, updateDTO);

        PostResponseDTO updatedPost = postService.updatePost(postId, updateDTO);
        return ResponseEntity.ok(updatedPost);
    }

    @Operation(summary = "게시물 삭제", description = "게시물을 삭제합니다.")
    @DeleteMapping("/{postId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId) {
        log.info("게시물 삭제 요청: postId={}", postId);

        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "게시물 좋아요 토글", description = "게시물의 좋아요 상태를 토글합니다.")
    @PostMapping("/{postId}/likes/toggle")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PostLikeResponseDTO> toggleLike(
            @PathVariable Long postId) {
        log.info("게시물 좋아요 토글 요청: postId={}", postId);

        PostLikeResponseDTO response = postService.toggleLike(postId);
        return ResponseEntity.ok(response);
    }


}
