package org.lucky0111.pettalk.repository.common;

import org.lucky0111.pettalk.domain.entity.common.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
}
