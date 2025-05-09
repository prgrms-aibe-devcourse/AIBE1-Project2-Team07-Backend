package org.lucky0111.pettalk.service.community;

import org.lucky0111.pettalk.domain.common.PetCategory;
import org.lucky0111.pettalk.domain.common.PostCategory;
import org.lucky0111.pettalk.domain.common.SortType;
import org.lucky0111.pettalk.domain.dto.community.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PostService {
    PostResponseDTO createPost(PostRequestDTO requestDTO, MultipartFile[] files) throws IOException;
    PostPageDTO getAllPosts(int page, PostCategory postCategory, PetCategory petCategory, SortType sortType);
    PostPageDTO searchPosts(String keyword, int page, PostCategory postCategory, PetCategory petCategory, SortType sortType);
    List<PostResponseDTO> getMyPosts();
    List<PostResponseDTO> getLikedPosts();
    PostResponseDTO getPostById(Long postId);
    PostResponseDTO updatePost(Long postId, PostUpdateDTO updateDTO);
    void deletePost(Long postId);
    PostLikeResponseDTO toggleLike(Long postId);

}
