package org.lucky0111.pettalk.repository.community;

import org.lucky0111.pettalk.domain.common.PetCategory;
import org.lucky0111.pettalk.domain.common.PostCategory;
import org.lucky0111.pettalk.domain.entity.community.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByPostCategory(PostCategory postCategory, Pageable pageable);
    Page<Post> findByPetCategory(PetCategory petCategory, Pageable pageable);
    Page<Post> findByPostCategoryAndPetCategory(
            PostCategory postCategory, PetCategory petCategory, Pageable pageable);
    

    @Query("SELECT p FROM Post p LEFT JOIN PostLike pl ON p.postId = pl.post.postId " +
            "WHERE (:postCategory IS NULL OR p.postCategory = :postCategory) " +
            "AND (:petCategory IS NULL OR p.petCategory = :petCategory) " +
            "GROUP BY p.postId ORDER BY COUNT(pl.likeId) DESC")
    Page<Post> findByFiltersOrderByLikes(
            PostCategory postCategory, PetCategory petCategory, Pageable pageable);

    // 사용자별 조회
    List<Post> findByUser_UserId(UUID userId);

    // 제목 검색
    Page<Post> findByTitleContaining(String keyword, Pageable pageable);

    // 내용 검색
    Page<Post> findByContentContaining(String keyword, Pageable pageable);

    // 제목 또는 내용 검색 (OR 조건)
    Page<Post> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);

    // 카테고리와 함께 검색
    Page<Post> findByPostCategoryAndTitleContaining(
            PostCategory postCategory, String keyword, Pageable pageable);

    Page<Post> findAll(Specification<Post> postSpecification, Pageable pageable);
}
