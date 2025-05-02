package org.lucky0111.pettalk.repository.community;

import org.lucky0111.pettalk.domain.entity.community.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
