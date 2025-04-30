package org.lucky0111.pettalk.repository.community;

import org.lucky0111.pettalk.domain.entity.community.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
