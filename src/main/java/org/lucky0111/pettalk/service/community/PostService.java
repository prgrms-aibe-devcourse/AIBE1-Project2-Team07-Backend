package org.lucky0111.pettalk.service.community;

import org.lucky0111.pettalk.domain.common.PetCategory;
import org.lucky0111.pettalk.domain.common.PostCategory;
import org.lucky0111.pettalk.domain.common.SortType;
import org.lucky0111.pettalk.domain.dto.community.PostLikeResponseDTO;
import org.lucky0111.pettalk.domain.dto.community.PostRequestDTO;
import org.lucky0111.pettalk.domain.dto.community.PostResponseDTO;
import org.lucky0111.pettalk.domain.dto.community.PostUpdateDTO;

import java.util.List;

public interface PostService {
    PostResponseDTO createPost(PostRequestDTO requestDTO);
    List<PostResponseDTO> getAllPosts(int page, PostCategory postCategory, PetCategory petCategory, SortType sortType);
    List<PostResponseDTO> searchPosts(String keyword, int page, PostCategory postCategory, PetCategory petCategory, SortType sortType);
    List<PostResponseDTO> getMyPosts();
    List<PostResponseDTO> getLikedPosts();
    PostResponseDTO getPostById(Long postId);
    PostResponseDTO updatePost(Long postId, PostUpdateDTO updateDTO);
    void deletePost(Long postId);
    PostLikeResponseDTO toggleLike(Long postId);

}
