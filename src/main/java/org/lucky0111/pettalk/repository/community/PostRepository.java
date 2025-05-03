package org.lucky0111.pettalk.repository.community;

import org.lucky0111.pettalk.domain.entity.community.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 카테고리별 조회 메서드
    Page<Post> findByPostCategory_PostCategoryId(Long postCategoryId, Pageable pageable);
    Page<Post> findByPetCategory_PetCategoryId(Long petCategoryId, Pageable pageable);
    Page<Post> findByPostCategory_PostCategoryIdAndPetCategory_PetCategoryId(
            Long postCategoryId, Long petCategoryId, Pageable pageable);

    // 사용자별 조회
    List<Post> findByUser_UserId(UUID userId);
}
