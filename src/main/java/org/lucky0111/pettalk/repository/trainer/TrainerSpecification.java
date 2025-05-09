package org.lucky0111.pettalk.repository.trainer;

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

            if (sortType == TrainerSortType.LATEST) {
                query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            }

            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Trainer> withKeywordAndSort(
            String keyword, TrainerSearchType searchType, TrainerSortType sortType) {
        return Specification
                .where(searchByKeyword(keyword, searchType))
                .and(withSortType(sortType));
    }
}