package org.lucky0111.pettalk.repository.trainer;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.lucky0111.pettalk.domain.common.TrainerSearchType;
import org.lucky0111.pettalk.domain.common.TrainerSortType;
import org.lucky0111.pettalk.domain.entity.trainer.Trainer;
import org.springframework.data.jpa.domain.Specification;

public class TrainerSpecification {
    public static Specification<Trainer> searchByKeyword(String keyword, TrainerSearchType searchType) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + keyword.trim().toLowerCase() + "%";

            switch (searchType) {
                case TITLE:
                    return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern);
                case CONTENT:
                    return criteriaBuilder.like(criteriaBuilder.lower(root.get("introduction")), pattern);
                case LOCATION:
                    return criteriaBuilder.like(criteriaBuilder.lower(root.get("visitingAreas")), pattern);
                case ALL:
                default:
                    return criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("introduction")), pattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("visitingAreas")), pattern)
                    );
            }
        };
    }

    public static Specification<Trainer> withSortType(TrainerSortType sortType) {
        return (root, query, criteriaBuilder) -> {
            if (query.getResultType() == Long.class || query.getResultType() == long.class) {
                return criteriaBuilder.conjunction();
            }

            applySortOrder(root, query, criteriaBuilder, sortType);
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Trainer> withKeywordAndSort(
            String keyword, TrainerSearchType searchType, TrainerSortType sortType) {
        return Specification
                .where(searchByKeyword(keyword, searchType))
                .and(withSortType(sortType));
    }

    private static void applySortOrder(
            Root<Trainer> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder,
            TrainerSortType sortType) {

        if (sortType == null) {
            sortType = TrainerSortType.LATEST;
        }

        switch (sortType) {
            case LATEST:
                query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
                break;

            case REVIEWS:
            case RATING:
            default:
                query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
                break;
        }
    }
}