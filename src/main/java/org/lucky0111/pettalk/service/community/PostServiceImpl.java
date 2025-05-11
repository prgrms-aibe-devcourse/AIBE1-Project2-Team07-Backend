package org.lucky0111.pettalk.service.community;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.common.ErrorCode;
import org.lucky0111.pettalk.domain.common.SortType;
import org.lucky0111.pettalk.domain.dto.auth.CustomOAuth2User;
import org.lucky0111.pettalk.domain.dto.community.*;
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
import org.lucky0111.pettalk.service.file.FileUploaderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final FileUploaderService fileUploaderService;

    private static final int PAGE_SIZE = 5;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public PostResponseDTO createPost(PostRequestDTO requestDTO, MultipartFile[] files, MultipartFile video) throws IOException {
        PetUser currentUser = getCurrentUser();

        Post post = buildPostFromRequest(requestDTO, currentUser);

        String folderName = "post/";
        if (files != null)
        {
            log.info("files is not null");
            for (MultipartFile file : files) {
                String imgUrl = fileUploaderService.uploadFile(file, folderName);

                PostImage postImage = new PostImage();
                postImage.setPost(post);
                postImage.setImageUrl(imgUrl);
                postImageRepository.save(postImage);
            }
        }

        if (video != null && !video.isEmpty()) {
            log.info("video file is not empty");
            String videoUrl = fileUploaderService.uploadFile(video, "video/");
            post.setVideoUrl(videoUrl);
        }

        Post savedPost = postRepository.save(post);

        savePostTags(savedPost, requestDTO.tagIds());

        return convertToResponseDTO(savedPost, currentUser.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponseDTO> getMyPosts() {
        UUID currentUserUUID = getCurrentUserUUID();

        List<Post> posts = postRepository.findByUser_UserId(currentUserUUID);

        return processPostsToResponses(posts, currentUserUUID);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponseDTO> getLikedPosts() {
        UUID currentUserUUID = getCurrentUserUUID();

        List<Post> posts = postLikeRepository.findPostsByUserId(currentUserUUID);

        return processPostsToResponses(posts, currentUserUUID);
    }


    @Override
    @Transactional(readOnly = true)
    public PostPageDTO searchPosts(
            String keyword, int page, int size, PostCategory postCategory,
            PetCategory petCategory, SortType sortType) {

        UUID currentUserUUID = getCurrentUserUUIDOrNull();

        Pageable pageable = PageRequest.of(page, size);

        Specification<Post> spec = PostSpecification.withFiltersAndSort(keyword, postCategory, petCategory, sortType);

        Page<Post> postsPage = postRepository.findAll(spec, pageable);
        List<Post> posts = postsPage.getContent();

        List<PostResponseDTO> postResponses = processPostsToResponses(posts, currentUserUUID);

        return new PostPageDTO(postResponses, page, postsPage.getSize(), postsPage.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponseDTO getPostById(Long postId) {
        UUID currentUserUUID = getCurrentUserUUIDOrNull();
        Post post = findPostById(postId);

        boolean hasLiked = checkUserLikedPost(post, currentUserUUID);
        List<String> tags = fetchPostTags(postId);

        return buildPostResponse(post, currentUserUUID, post.getLikeCount(), post.getCommentCount(), hasLiked, tags);
    }

    @Override
    @Transactional
    public PostResponseDTO updatePost(Long postId, PostUpdateDTO updateDTO, MultipartFile[] files, MultipartFile video) throws IOException {
        UUID currentUserUUID = getCurrentUserUUID();
        Post post = findPostById(postId);

        validatePostOwnership(post, currentUserUUID);
        updatePostFromDTO(post, updateDTO);

        if (updateDTO.deleteImageUrls() != null && !updateDTO.deleteImageUrls().isEmpty()) {
            String[] imageUrlsToDelete = updateDTO.deleteImageUrls().split(",");
            for (String imageUrl : imageUrlsToDelete) {
                postImageRepository.deleteByImageUrl(imageUrl.trim());
                fileUploaderService.deleteFile(imageUrl.trim());
            }
        }

        if (updateDTO.deleteVideo()) {
            if (post.getVideoUrl() != null) {
                // 실제 파일 시스템에서 비디오 삭제 (필요한 경우)
                fileUploaderService.deleteFile(post.getVideoUrl());
                post.setVideoUrl(null);
            }
        }


        String folderName = "post/";
        if (files != null)
        {
            log.info("files is not null");
            for (MultipartFile file : files) {
                String imgUrl = fileUploaderService.uploadFile(file, folderName);

                PostImage postImage = new PostImage();
                postImage.setPost(post);
                postImage.setImageUrl(imgUrl);
                postImageRepository.save(postImage);
            }
        }

        if (video != null && !video.isEmpty()) {
            log.info("video file is not empty");
            String videoUrl = fileUploaderService.uploadFile(video, "video/");
            post.setVideoUrl(videoUrl);
        }

        if (updateDTO.tagIds() != null) {
            postTagRepository.deleteByPost(post);
            postTagRepository.flush();
            savePostTags(post, updateDTO.tagIds());
        }

        Post updatedPost = postRepository.save(post);

        boolean hasLiked = false;
        List<String> tags = fetchPostTags(postId);

        return buildPostResponse(updatedPost, currentUserUUID, post.getLikeCount(), post.getCommentCount(), hasLiked, tags);
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
            post.decrementCLikeCount();
            postRepository.save(post);

            return handleLikeRemoval(existingLike.get(), post);
        } else {
            post.incrementLikeCount();
            postRepository.save(post);

            return handleLikeCreation(post, currentUser);
        }
    }

    private List<PostResponseDTO> processPostsToResponses(List<Post> posts, UUID currentUserUUID) {
        List<Long> postIds = extractPostIds(posts);
        Map<Long, Boolean> userLikedMap = fetchUserLikeStatusByPostIds(postIds, currentUserUUID);
        Map<Long, List<String>> postTagsMap = fetchTagsByPostIds(postIds);

        return posts.stream()
                .map(post -> buildPostResponse(
                        post,
                        currentUserUUID,
                        post.getLikeCount(),
                        post.getCommentCount(),
                        userLikedMap.getOrDefault(post.getPostId(), false),
                        postTagsMap.getOrDefault(post.getPostId(), new ArrayList<>())
                ))
                .collect(Collectors.toList());
    }

    private Post buildPostFromRequest(PostRequestDTO requestDTO, PetUser user) {
        Post post = new Post();
        post.setUser(user);
        post.setPostCategory(requestDTO.postCategory());
        post.setPetCategory(requestDTO.petCategory());
        post.setTitle(requestDTO.title());
        post.setContent(requestDTO.content());
        return post;
    }

    private void savePostTags(Post post, List<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(tagIds);
            List<PostTagRelation> postTags = new ArrayList<>();

            for (Tag tag : tags) {
                PostTagRelation postTag = new PostTagRelation();
                postTag.setPost(post);
                postTag.setTag(tag);
                postTags.add(postTag);
            }

            postTagRepository.saveAll(postTags);
        }
    }

    private List<Long> extractPostIds(List<Post> posts) {
        return posts.stream()
                .map(Post::getPostId)
                .collect(Collectors.toList());
    }

    private Map<Long, Boolean> fetchUserLikeStatusByPostIds(List<Long> postIds, UUID userId) {
        return postLikeRepository.checkUserLikeStatus(postIds, userId)
                .stream()
                .collect(Collectors.toMap(
                        PostLikeRepository.PostLikeStatusProjection::getPostId,
                        PostLikeRepository.PostLikeStatusProjection::getHasLiked
                ));
    }

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

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("해당 게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private boolean checkUserLikedPost(Post post, UUID userId) {
        return postLikeRepository.existsByPostAndUser_UserId(post, userId);
    }

    private List<String> fetchPostTags(Long postId) {
        return postTagRepository.findTagNamesByPostId(postId)
                .stream()
                .map(PostTagRepository.PostTagProjection::getTagName)
                .collect(Collectors.toList());
    }

    private void validatePostOwnership(Post post, UUID userId) {
        if (!post.getUser().getUserId().equals(userId)) {
            throw new CustomException("게시물에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
    }

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
                false
        );
    }

    private PostLikeResponseDTO handleLikeCreation(Post post, PetUser user) {
        PostLike postLike = new PostLike();
        postLike.setPost(post);
        postLike.setUser(user);

        PostLike savedLike = postLikeRepository.save(postLike);

        return new PostLikeResponseDTO(
                savedLike.getLikeId(),
                post.getPostId(),
                true
        );
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : null;
    }

    private PostResponseDTO convertToResponseDTO(Post post, UUID currentUserUUID) {
        boolean hasLiked = checkUserLikedPost(post, currentUserUUID);
        List<String> tags = fetchPostTags(post.getPostId());

        return buildPostResponse(post, currentUserUUID, post.getLikeCount(), post.getCommentCount(), hasLiked, tags);
    }

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

    private UUID getCurrentUserUUID() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomOAuth2User userDetails) {
            return userDetails.getUserId();
        }

        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    private UUID getCurrentUserUUIDOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomOAuth2User userDetails) {
            return userDetails.getUserId();
        }
        return null;
    }

    private PetUser getCurrentUser() {
        UUID currentUserUUID = getCurrentUserUUID();
        return petUserRepository.findById(currentUserUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}