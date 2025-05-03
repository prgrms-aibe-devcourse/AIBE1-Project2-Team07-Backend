package org.lucky0111.pettalk.service.community;

import jakarta.servlet.http.HttpServletRequest;
import org.lucky0111.pettalk.domain.dto.community.PostLikeResponseDTO;
import org.lucky0111.pettalk.domain.dto.community.PostRequestDTO;
import org.lucky0111.pettalk.domain.dto.community.PostResponseDTO;
import org.lucky0111.pettalk.domain.dto.community.PostUpdateDTO;

import java.util.List;

public interface PostService {
    PostResponseDTO createPost(PostRequestDTO requestDTO, HttpServletRequest request);
    List<PostResponseDTO> getAllPosts(int page, int size, Long postCategoryId, Long petCategoryId, HttpServletRequest request);
    PostResponseDTO getPostById(Long postId, HttpServletRequest request);
    PostResponseDTO updatePost(Long postId, PostUpdateDTO updateDTO, HttpServletRequest request);
    void deletePost(Long postId, HttpServletRequest request);
    PostLikeResponseDTO toggleLike(Long postId, HttpServletRequest request);
}
