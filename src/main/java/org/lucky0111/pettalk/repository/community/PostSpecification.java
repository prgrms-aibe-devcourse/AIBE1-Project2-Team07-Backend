package org.lucky0111.pettalk.repository.community;

import org.lucky0111.pettalk.domain.common.PetCategory;
import org.lucky0111.pettalk.domain.common.PostCategory;
import org.lucky0111.pettalk.domain.entity.community.Post;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecification {
    public static Specification<Post> withFilters(String keyword, PostCategory postCategory, PetCategory petCategory) {
        return Specification
                .where(withKeyword(keyword))
                .and(withPostCategory(postCategory))
                .and(withPetCategory(petCategory));
    }

    public static Specification<Post> withFilters(PostCategory postCategory, PetCategory petCategory) {
        return Specification
                .where(withPostCategory(postCategory))
                .and(withPetCategory(petCategory));
    }

    private static Specification<Post> withPostCategory(PostCategory postCategory) {
        return (root, query, criteriaBuilder) -> {
            if (postCategory == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("postCategory"), postCategory);
        };
    }

    private static Specification<Post> withPetCategory(PetCategory petCategory) {
        return (root, query, criteriaBuilder) -> {
            if (petCategory == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("petCategory"), petCategory);
        };
    }

    // PostSpecification.java에 검색 조건 추가
    public static Specification<Post> withKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + keyword + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("title"), pattern),
                    criteriaBuilder.like(root.get("content"), pattern)
            );
        };
    }

}