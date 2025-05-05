package org.lucky0111.pettalk.repository.community;

import org.lucky0111.pettalk.domain.common.PetCategory;
import org.lucky0111.pettalk.domain.common.PostCategory;
import org.lucky0111.pettalk.domain.entity.community.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 카테고리별 조회 메서드
    Page<Post> findByPostCategory(PostCategory postCategory, Pageable pageable);
    Page<Post> findByPetCategory(PetCategory petCategory, Pageable pageable);
    Page<Post> findByPostCategoryAndPetCategory(
            PostCategory postCategory, PetCategory petCategory, Pageable pageable);

    // 사용자별 조회
    List<Post> findByUser_UserId(UUID userId);
}
