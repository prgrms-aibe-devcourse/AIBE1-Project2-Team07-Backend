package org.lucky0111.pettalk.service.community;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.common.ErrorCode;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.dto.community.PostLikeResponseDTO;
import org.lucky0111.pettalk.domain.dto.community.PostRequestDTO;
import org.lucky0111.pettalk.domain.dto.community.PostResponseDTO;
import org.lucky0111.pettalk.domain.dto.community.PostUpdateDTO;
import org.lucky0111.pettalk.domain.entity.common.Tag;
import org.lucky0111.pettalk.domain.common.PetCategory;
import org.lucky0111.pettalk.domain.common.PostCategory;
import org.lucky0111.pettalk.domain.entity.community.Post;
import org.lucky0111.pettalk.domain.entity.community.PostImage;
import org.lucky0111.pettalk.domain.entity.community.PostLike;
import org.lucky0111.pettalk.domain.entity.community.PostTagRelation;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.exception.CustomException;
import org.lucky0111.pettalk.repository.common.TagRepository;
import org.lucky0111.pettalk.repository.community.*;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final PostImageRepository postImageRepository;

    private static final int PAGE_SIZE = 10;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public PostResponseDTO createPost(PostRequestDTO requestDTO) {
        PetUser currentUser = getCurrentUser();

        Post post = buildPostFromRequest(requestDTO, currentUser);
        Post savedPost = postRepository.save(post);

        if (requestDTO.imageUrls() != null) {
            for (int i = 0; i < requestDTO.imageUrls().size(); i++) {
                PostImage postImage = new PostImage();
                postImage.setImageUrl(requestDTO.imageUrls().get(i));
                postImage.setPost(savedPost);
                postImageRepository.save(postImage);
            }
        }

        savePostTags(savedPost, requestDTO.tagIds());

        return convertToResponseDTO(savedPost, currentUser.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponseDTO> getAllPosts(int page, PostCategory postCategory, PetCategory petCategory) {
        UUID currentUserUUID = getCurrentUserUUID();

        Page<Post> postsPage = fetchPostsWithFilters(page, postCategory, petCategory);
        List<Post> posts = postsPage.getContent();

        List<Long> postIds = extractPostIds(posts);

        Map<Long, Integer> likeCounts = fetchLikeCountsByPostIds(postIds);
        Map<Long, Integer> commentCounts = fetchCommentCountsByPostIds(postIds);
        Map<Long, Boolean> userLikedMap = fetchUserLikeStatusByPostIds(postIds, currentUserUUID);
        Map<Long, List<String>> postTagsMap = fetchTagsByPostIds(postIds);

        return posts.stream()
                .map(post -> buildPostResponse(
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
    public PostResponseDTO getPostById(Long postId) {
        UUID currentUserUUID = getCurrentUserUUID();
        Post post = findPostById(postId);

        int likeCount = countPostLikes(post);
        int commentCount = countPostComments(post);
        boolean hasLiked = checkUserLikedPost(post, currentUserUUID);
        List<String> tags = fetchPostTags(postId);

        return buildPostResponse(post, currentUserUUID, likeCount, commentCount, hasLiked, tags);
    }

    @Override
    @Transactional
    public PostResponseDTO updatePost(Long postId, PostUpdateDTO updateDTO) {
        UUID currentUserUUID = getCurrentUserUUID();
        Post post = findPostById(postId);

        validatePostOwnership(post, currentUserUUID);
        updatePostFromDTO(post, updateDTO);

        if (updateDTO.tagIds() != null) {
            updatePostTags(post, updateDTO.tagIds());
        }

        Post updatedPost = postRepository.save(post);

        int likeCount = countPostLikes(updatedPost);
        int commentCount = countPostComments(updatedPost);
        boolean hasLiked = checkUserLikedPost(updatedPost, currentUserUUID);
        List<String> tags = fetchPostTags(postId);

        return buildPostResponse(updatedPost, currentUserUUID, likeCount, commentCount, hasLiked, tags);
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        UUID currentUserUUID = getCurrentUserUUID();
        Post post = findPostById(postId);

        validatePostOwnership(post, currentUserUUID);
        deletePostRelatedData(post);
        postRepository.delete(post);
    }

    @Override
    @Transactional
    public PostLikeResponseDTO toggleLike(Long postId) {
        PetUser currentUser = getCurrentUser();
        Post post = findPostById(postId);

        Optional<PostLike> existingLike = findExistingLike(post, currentUser);

        if (existingLike.isPresent()) {
            return handleLikeRemoval(existingLike.get(), post);
        } else {
            return handleLikeCreation(post, currentUser);
        }
    }

    // 게시물 생성 및 변환 관련 메소드
    private Post buildPostFromRequest(PostRequestDTO requestDTO, PetUser user) {
        Post post = new Post();
        post.setUser(user);
        post.setPostCategory(requestDTO.postCategory());
        post.setPetCategory(requestDTO.petCategory());
        post.setTitle(requestDTO.title());
        post.setContent(requestDTO.content());
        post.setVideoUrl(requestDTO.videoUrl());
        return post;
    }

    // 태그 저장 메소드
    private void savePostTags(Post post, List<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(tagIds);

            for (Tag tag : tags) {
                PostTagRelation postTag = new PostTagRelation();
                postTag.setPost(post);
                postTag.setTag(tag);
                postTagRepository.save(postTag);
            }
        }
    }

    // 게시물 조회 관련 메소드
    private Page<Post> fetchPostsWithFilters(int page, PostCategory postCategory, PetCategory petCategory) {
        Pageable pageable = createPageable(page);

        if (postCategory != null && petCategory != null) {
            return postRepository.findByPostCategoryAndPetCategory(postCategory, petCategory, pageable);
        } else if (postCategory != null) {
            return postRepository.findByPostCategory(postCategory, pageable);
        } else if (petCategory != null) {
            return postRepository.findByPetCategory(petCategory, pageable);
        } else {
            return postRepository.findAll(pageable);
        }
    }

    // 페이지 설정 생성 메소드
    private Pageable createPageable(int page) {
        return PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
    }

    // 게시물 ID 목록 추출 메소드
    private List<Long> extractPostIds(List<Post> posts) {
        return posts.stream()
                .map(Post::getPostId)
                .collect(Collectors.toList());
    }

    // 좋아요 개수 조회 메소드
    private Map<Long, Integer> fetchLikeCountsByPostIds(List<Long> postIds) {
        return postLikeRepository.countLikesByPostIds(postIds)
                .stream()
                .collect(Collectors.toMap(
                        PostLikeRepository.PostLikeCountProjection::getPostId,
                        PostLikeRepository.PostLikeCountProjection::getLikeCount
                ));
    }

    // 댓글 개수 조회 메소드
    private Map<Long, Integer> fetchCommentCountsByPostIds(List<Long> postIds) {
        return commentRepository.countCommentsByPostIds(postIds)
                .stream()
                .collect(Collectors.toMap(
                        CommentRepository.CommentCountProjection::getPostId,
                        CommentRepository.CommentCountProjection::getCommentCount
                ));
    }

    // 사용자 좋아요 여부 조회 메소드
    private Map<Long, Boolean> fetchUserLikeStatusByPostIds(List<Long> postIds, UUID userId) {
        return postLikeRepository.checkUserLikeStatus(postIds, userId)
                .stream()
                .collect(Collectors.toMap(
                        PostLikeRepository.PostLikeStatusProjection::getPostId,
                        PostLikeRepository.PostLikeStatusProjection::getHasLiked
                ));
    }

    // 태그 조회 메소드
    private Map<Long, List<String>> fetchTagsByPostIds(List<Long> postIds) {
        return postTagRepository.findTagNamesByPostIds(postIds)
                .stream()
                .collect(Collectors.groupingBy(
                        PostTagRepository.PostTagProjection::getPostId,
                        Collectors.mapping(
                                PostTagRepository.PostTagProjection::getTagName,
                                Collectors.toList()
                        )
                ));
    }

    // 게시물 ID로 게시물 찾기
    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("해당 게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    // 게시물 좋아요 개수 조회
    private int countPostLikes(Post post) {
        return postLikeRepository.countByPost(post);
    }

    // 게시물 댓글 개수 조회
    private int countPostComments(Post post) {
        return commentRepository.countByPost(post);
    }

    // 사용자 게시물 좋아요 여부 확인
    private boolean checkUserLikedPost(Post post, UUID userId) {
        return postLikeRepository.existsByPostAndUser_UserId(post, userId);
    }

    // 게시물 태그 목록 조회
    private List<String> fetchPostTags(Long postId) {
        return postTagRepository.findTagNamesByPostId(postId)
                .stream()
                .map(PostTagRepository.PostTagProjection::getTagName)
                .collect(Collectors.toList());
    }

    // 게시물 소유권 검증
    private void validatePostOwnership(Post post, UUID userId) {
        if (!post.getUser().getUserId().equals(userId)) {
            throw new CustomException("게시물에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
    }

    // 게시물 업데이트
    private void updatePostFromDTO(Post post, PostUpdateDTO updateDTO) {
        if (updateDTO.postCategory() != null) {
            post.setPostCategory(updateDTO.postCategory());
        }

        if (updateDTO.petCategory() != null) {
            post.setPetCategory(updateDTO.petCategory());
        }

        if (updateDTO.title() != null) {
            post.setTitle(updateDTO.title());
        }

        if (updateDTO.content() != null) {
            post.setContent(updateDTO.content());
        }

        if (updateDTO.videoUrl() != null) {
            post.setVideoUrl(updateDTO.videoUrl());
        }
    }

    // 게시물 태그 업데이트
    private void updatePostTags(Post post, List<Long> tagIds) {
        postTagRepository.deleteByPost(post);

        List<Tag> tags = tagRepository.findAllById(tagIds);
        for (Tag tag : tags) {
            PostTagRelation postTag = new PostTagRelation();
            postTag.setPost(post);
            postTag.setTag(tag);
            postTagRepository.save(postTag);
        }
    }

    private void deletePostRelatedData(Post post) {
        postTagRepository.deleteByPost(post);
        postLikeRepository.deleteByPost(post);
        postImageRepository.deleteByPost(post);
        commentRepository.deleteByPost(post);
    }

    private Optional<PostLike> findExistingLike(Post post, PetUser user) {
        return postLikeRepository.findByPostAndUser(post, user);
    }

    private PostLikeResponseDTO handleLikeRemoval(PostLike existingLike, Post post) {
        postLikeRepository.delete(existingLike);

        return new PostLikeResponseDTO(
                existingLike.getLikeId(),
                post.getPostId(),
                null,
                false
        );
    }

    // 좋아요 생성 처리
    private PostLikeResponseDTO handleLikeCreation(Post post, PetUser user) {
        PostLike postLike = new PostLike();
        postLike.setPost(post);
        postLike.setUser(user);

        PostLike savedLike = postLikeRepository.save(postLike);
        String createdAt = formatCurrentDateTime();

        return new PostLikeResponseDTO(
                savedLike.getLikeId(),
                post.getPostId(),
                createdAt,
                true
        );
    }

    private String formatCurrentDateTime() {
        return java.time.LocalDateTime.now().format(DATE_FORMATTER);
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : null;
    }

    private PostResponseDTO convertToResponseDTO(Post post, UUID currentUserUUID) {
        int likeCount = countPostLikes(post);
        int commentCount = countPostComments(post);
        boolean hasLiked = checkUserLikedPost(post, currentUserUUID);
        List<String> tags = fetchPostTags(post.getPostId());

        return buildPostResponse(post, currentUserUUID, likeCount, commentCount, hasLiked, tags);
    }

    // PostResponseDTO 변환 및 생성 메소드 (오버로딩 2)
    private PostResponseDTO buildPostResponse(Post post, UUID currentUserUUID,
                                              int likeCount, int commentCount,
                                              boolean hasLiked, List<String> tags) {
        List<String> imageUrls = post.getImages().stream()
                .map(PostImage::getImageUrl)
                .toList();

        String createdAt = formatDateTime(post.getCreatedAt());
        String updatedAt = formatDateTime(post.getUpdatedAt());

        return new PostResponseDTO(
                post.getPostId(),
                post.getUser().getName(),
                post.getUser().getNickname(),
                post.getUser().getProfileImageUrl(),
                post.getPostCategory(),
                post.getPetCategory(),
                post.getTitle(),
                post.getContent(),
                imageUrls,
                post.getVideoUrl(),
                likeCount,
                commentCount,
                hasLiked,
                tags,
                createdAt,
                updatedAt
        );
    }

    // 현재 사용자 UUID 가져오기
    private UUID getCurrentUserUUID() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomOAuth2User userDetails) {
            return userDetails.getUserId();
        }

        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    // 현재 사용자 정보 가져오기
    private PetUser getCurrentUser() {
        UUID currentUserUUID = getCurrentUserUUID();
        return petUserRepository.findById(currentUserUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}