package org.lucky0111.pettalk.service.community;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.dto.community.PostLikeResponseDTO;
import org.lucky0111.pettalk.domain.dto.community.PostRequestDTO;
import org.lucky0111.pettalk.domain.dto.community.PostResponseDTO;
import org.lucky0111.pettalk.domain.dto.community.PostUpdateDTO;
import org.lucky0111.pettalk.domain.entity.common.Tag;
import org.lucky0111.pettalk.domain.entity.common.PetCategory;
import org.lucky0111.pettalk.domain.entity.common.PostCategory;
import org.lucky0111.pettalk.domain.entity.community.Post;
import org.lucky0111.pettalk.domain.entity.community.PostLike;
import org.lucky0111.pettalk.domain.entity.community.PostTagRelation;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.exception.CustomException;
import org.lucky0111.pettalk.repository.common.TagRepository;
import org.lucky0111.pettalk.repository.community.*;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.lucky0111.pettalk.util.auth.JWTUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PetUserRepository petUserRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final PetCategoryRepository petCategoryRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final JWTUtil jwtUtil;

    @Override
    @Transactional
    public PostResponseDTO createPost(PostRequestDTO requestDTO, HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);
        PetUser currentUser = getCurrentUser(request);

        PostCategory postCategory = postCategoryRepository.findById(requestDTO.postCategoryId())
                .orElseThrow(() -> new CustomException("해당 게시판 카테고리를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST));

        PetCategory petCategory = petCategoryRepository.findById(requestDTO.petCategoryId())
                .orElseThrow(() -> new CustomException("해당 반려동물 카테고리를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST));

        // 게시물 생성
        Post post = new Post();
        post.setUser(currentUser);
        post.setPostCategory(postCategory);
        post.setPetCategory(petCategory);
        post.setTitle(requestDTO.title());
        post.setContent(requestDTO.content());
        post.setImageUrl(requestDTO.imageUrl());
        post.setVideoUrl(requestDTO.videoUrl());

        Post savedPost = postRepository.save(post);

        // 태그 처리
        if (requestDTO.tagIds() != null && !requestDTO.tagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(requestDTO.tagIds());

            for (Tag tag : tags) {
                PostTagRelation postTag = new PostTagRelation();
                postTag.setPost(savedPost);
                postTag.setTag(tag);
                postTagRepository.save(postTag);
            }
        }

        return convertToResponseDTO(savedPost, currentUserUUID);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponseDTO> getAllPosts(int page, int size, Long postCategoryId, Long petCategoryId, HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);

        // 정렬 조건 (최신순)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Post> postsPage;

        // 카테고리 필터링 조건
        if (postCategoryId != null && petCategoryId != null) {
            postsPage = postRepository.findByPostCategory_PostCategoryIdAndPetCategory_PetCategoryId(
                    postCategoryId, petCategoryId, pageable);
        } else if (postCategoryId != null) {
            postsPage = postRepository.findByPostCategory_PostCategoryId(postCategoryId, pageable);
        } else if (petCategoryId != null) {
            postsPage = postRepository.findByPetCategory_PetCategoryId(petCategoryId, pageable);
        } else {
            postsPage = postRepository.findAll(pageable);
        }

        List<Post> posts = postsPage.getContent();

        // 일괄 처리를 위한 게시물 ID 목록
        List<Long> postIds = posts.stream()
                .map(Post::getPostId)
                .collect(Collectors.toList());

        // 좋아요 개수 조회
        Map<Long, Integer> likeCounts = postLikeRepository.countLikesByPostIds(postIds)
                .stream()
                .collect(Collectors.toMap(
                        PostLikeRepository.PostLikeCountProjection::getPostId,
                        PostLikeRepository.PostLikeCountProjection::getLikeCount
                ));

        // 댓글 개수 조회
        Map<Long, Integer> commentCounts = commentRepository.countCommentsByPostIds(postIds)
                .stream()
                .collect(Collectors.toMap(
                        CommentRepository.CommentCountProjection::getPostId,
                        CommentRepository.CommentCountProjection::getCommentCount
                ));

        // 사용자의 좋아요 여부 조회
        Map<Long, Boolean> userLikedMap = postLikeRepository.checkUserLikeStatus(
                        postIds, currentUserUUID)
                .stream()
                .collect(Collectors.toMap(
                        PostLikeRepository.PostLikeStatusProjection::getPostId,
                        PostLikeRepository.PostLikeStatusProjection::getHasLiked
                ));

        // 태그 조회
        Map<Long, List<String>> postTagsMap = postTagRepository.findTagNamesByPostIds(postIds)
                .stream()
                .collect(Collectors.groupingBy(
                        PostTagRepository.PostTagProjection::getPostId,
                        Collectors.mapping(
                                PostTagRepository.PostTagProjection::getTagName,
                                Collectors.toList()
                        )
                ));

        return posts.stream()
                .map(post -> convertToResponseDTO(
                        post,
                        currentUserUUID,
                        likeCounts.getOrDefault(post.getPostId(), 0),
                        commentCounts.getOrDefault(post.getPostId(), 0),
                        userLikedMap.getOrDefault(post.getPostId(), false),
                        postTagsMap.getOrDefault(post.getPostId(), new ArrayList<>())
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponseDTO getPostById(Long postId, HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("해당 게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 좋아요 개수
        int likeCount = postLikeRepository.countByPost(post);

        // 댓글 개수
        int commentCount = commentRepository.countByPost(post);

        // 사용자 좋아요 여부
        boolean hasLiked = postLikeRepository.existsByPostAndUser_UserId(post, currentUserUUID);

        // 태그 목록
        List<String> tags = postTagRepository.findTagNamesByPostId(postId)
                .stream()
                .map(PostTagRepository.PostTagProjection::getTagName)
                .collect(Collectors.toList());

        return convertToResponseDTO(post, currentUserUUID, likeCount, commentCount, hasLiked, tags);
    }

    @Override
    @Transactional
    public PostResponseDTO updatePost(Long postId, PostUpdateDTO updateDTO, HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("해당 게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 작성자 확인
        if (!post.getUser().getUserId().equals(currentUserUUID)) {
            throw new CustomException("게시물을 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        // 게시판 카테고리 변경
        if (updateDTO.postCategoryId() != null) {
            PostCategory postCategory = postCategoryRepository.findById(updateDTO.postCategoryId())
                    .orElseThrow(() -> new CustomException("해당 게시판 카테고리를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST));
            post.setPostCategory(postCategory);
        }

        // 반려동물 카테고리 변경
        if (updateDTO.petCategoryId() != null) {
            PetCategory petCategory = petCategoryRepository.findById(updateDTO.petCategoryId())
                    .orElseThrow(() -> new CustomException("해당 반려동물 카테고리를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST));
            post.setPetCategory(petCategory);
        }

        // 내용 수정
        if (updateDTO.title() != null) {
            post.setTitle(updateDTO.title());
        }

        if (updateDTO.content() != null) {
            post.setContent(updateDTO.content());
        }

        if (updateDTO.imageUrl() != null) {
            post.setImageUrl(updateDTO.imageUrl());
        }

        if (updateDTO.videoUrl() != null) {
            post.setVideoUrl(updateDTO.videoUrl());
        }

        // 태그 업데이트
        if (updateDTO.tagIds() != null) {
            // 기존 태그 삭제
            postTagRepository.deleteByPost(post);

            // 새 태그 추가
            List<Tag> tags = tagRepository.findAllById(updateDTO.tagIds());
            for (Tag tag : tags) {
                PostTagRelation postTag = new PostTagRelation();
                postTag.setPost(post);
                postTag.setTag(tag);
                postTagRepository.save(postTag);
            }
        }

        Post updatedPost = postRepository.save(post);

        // 좋아요 개수
        int likeCount = postLikeRepository.countByPost(updatedPost);

        // 댓글 개수
        int commentCount = commentRepository.countByPost(updatedPost);

        // 사용자 좋아요 여부
        boolean hasLiked = postLikeRepository.existsByPostAndUser_UserId(updatedPost, currentUserUUID);

        // 태그 목록
        List<String> tags = postTagRepository.findTagNamesByPostId(postId)
                .stream()
                .map(PostTagRepository.PostTagProjection::getTagName)
                .collect(Collectors.toList());

        return convertToResponseDTO(updatedPost, currentUserUUID, likeCount, commentCount, hasLiked, tags);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("해당 게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 작성자 확인
        if (!post.getUser().getUserId().equals(currentUserUUID)) {
            throw new CustomException("게시물을 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        // 관련 태그 삭제
        postTagRepository.deleteByPost(post);

        // 관련 좋아요 삭제
        postLikeRepository.deleteByPost(post);

        // 관련 댓글 삭제
        commentRepository.deleteByPost(post);

        // 게시물 삭제
        postRepository.delete(post);
    }

    public PostLikeResponseDTO toggleLike(Long postId, HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);
        PetUser currentUser = getCurrentUser(request);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물을 찾을 수 없습니다."));

        // 이미 좋아요가 있는지 확인
        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, currentUser);

        // 좋아요가 이미 있으면 삭제, 없으면 생성
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());

            // 좋아요 취소 응답
            return new PostLikeResponseDTO(
                    existingLike.get().getLikeId(),
                    post.getPostId(),
                    currentUserUUID,
                    null, // 생성 시간 없음 (삭제됨)
                    false // 좋아요 상태: 취소됨
            );
        } else {
            // 좋아요 생성
            PostLike postLike = new PostLike();
            postLike.setPost(post);
            postLike.setUser(currentUser);

            PostLike savedLike = postLikeRepository.save(postLike);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String createdAt = java.time.LocalDateTime.now().format(formatter);

            // 좋아요 추가 응답
            return new PostLikeResponseDTO(
                    savedLike.getLikeId(),
                    post.getPostId(),
                    currentUserUUID,
                    createdAt,
                    true // 좋아요 상태: 추가됨
            );
        }
    }

    // Post 엔티티를 PostResponseDTO로 변환
    private PostResponseDTO convertToResponseDTO(Post post, UUID currentUserUUID) {
        // 좋아요 개수
        int likeCount = postLikeRepository.countByPost(post);

        // 댓글 개수
        int commentCount = commentRepository.countByPost(post);

        // 사용자 좋아요 여부
        boolean hasLiked = postLikeRepository.existsByPostAndUser_UserId(post, currentUserUUID);

        // 태그 목록
        List<String> tags = postTagRepository.findTagNamesByPostId(post.getPostId())
                .stream()
                .map(PostTagRepository.PostTagProjection::getTagName)
                .collect(Collectors.toList());

        return convertToResponseDTO(post, currentUserUUID, likeCount, commentCount, hasLiked, tags);
    }

    // Post 엔티티를 PostResponseDTO로 변환 (상세 정보 포함)
    private PostResponseDTO convertToResponseDTO(Post post, UUID currentUserUUID,
                                                 int likeCount, int commentCount,
                                                 boolean hasLiked, List<String> tags) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String createdAt = post.getCreatedAt() != null ?
                post.getCreatedAt().format(formatter) : null;

        String updatedAt = post.getUpdatedAt() != null ?
                post.getUpdatedAt().format(formatter) : null;

        return new PostResponseDTO(
                post.getPostId(),
                post.getUser().getUserId(),
                post.getUser().getName(),
                post.getUser().getNickname(),
                post.getUser().getProfileImageUrl(),
                post.getPostCategory().getPostCategoryName(),
                post.getPetCategory().getPetCategoryName(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrl(),
                post.getVideoUrl(),
                likeCount,
                commentCount,
                hasLiked,
                tags,
                createdAt,
                updatedAt
        );
    }

    // JWT 토큰에서 현재 사용자 UUID 추출
    private UUID getCurrentUserUUID(HttpServletRequest request) {
        String token = extractJwtToken(request);
        if (token == null) {
            throw new CustomException("인증 토큰을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED);
        }
        return jwtUtil.getUserId(token);
    }

    // 현재 사용자 엔티티 조회
    private PetUser getCurrentUser(HttpServletRequest request) {
        UUID currentUserUUID = getCurrentUserUUID(request);
        return petUserRepository.findById(currentUserUUID)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED));
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