package org.lucky0111.pettalk.domain.common;

import org.springframework.data.domain.Sort;

public enum SortType {
    LATEST {
        @Override
        public Sort getSort() {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    },
    OLDEST {
        @Override
        public Sort getSort() {
            return Sort.by(Sort.Direction.ASC, "createdAt");
        }
    },
    LIKES {
        @Override
        public Sort getSort() {
            return Sort.by(Sort.Direction.DESC, "likeCount");
        }
    },
    COMMENTS {
        @Override
        public Sort getSort() {
            return Sort.by(Sort.Direction.DESC, "commentCount");
        }
    };

    public abstract Sort getSort();
}
