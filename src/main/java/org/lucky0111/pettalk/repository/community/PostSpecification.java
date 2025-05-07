package org.lucky0111.pettalk.repository.community;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import org.lucky0111.pettalk.domain.common.PetCategory;
import org.lucky0111.pettalk.domain.common.PostCategory;
import org.lucky0111.pettalk.domain.common.SortType;
import org.lucky0111.pettalk.domain.entity.community.Post;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PostSpecification {
    public static Specification<Post> withPostCategory(PostCategory postCategory) {
        return (root, query, criteriaBuilder) -> {
            if (postCategory == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("postCategory"), postCategory);
        };
    }

    public static Specification<Post> withPetCategory(PetCategory petCategory) {
        return (root, query, criteriaBuilder) -> {
            if (petCategory == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("petCategory"), petCategory);
        };
    }

    public static Specification<Post> withKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + keyword.trim() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("title"), pattern),
                    criteriaBuilder.like(root.get("content"), pattern)
            );
        };
    }

    public static Specification<Post> withSortType(SortType sortType) {
        return (root, query, criteriaBuilder) -> {
            if (query.getResultType() == Long.class || query.getResultType() == long.class) {
                return criteriaBuilder.conjunction();
            }

            applySortOrder(root, query, criteriaBuilder, sortType);
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Post> withFiltersAndSort(
            String keyword, PostCategory postCategory, PetCategory petCategory, SortType sortType) {
        return Specification
                .where(withKeyword(keyword))
                .and(withPostCategory(postCategory))
                .and(withPetCategory(petCategory))
                .and(withSortType(sortType));
    }

    private static void applySortOrder(
            Root<Post> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder,
            SortType sortType) {

        if (sortType == null) {
            sortType = SortType.LATEST;
        }

        List<Order> orders = new ArrayList<>();

        switch (sortType) {
            case OLDEST:
                orders.add(criteriaBuilder.asc(root.get("createdAt")));
                break;
            case LIKES:
                orders.add(criteriaBuilder.desc(root.get("likeCount")));
                orders.add(criteriaBuilder.desc(root.get("createdAt")));
                break;
            case COMMENTS:
                orders.add(criteriaBuilder.desc(root.get("commentCount")));
                orders.add(criteriaBuilder.desc(root.get("createdAt")));
                break;
            case LATEST:
            default:
                orders.add(criteriaBuilder.desc(root.get("createdAt")));
                break;
        }

        query.orderBy(orders);
    }
}