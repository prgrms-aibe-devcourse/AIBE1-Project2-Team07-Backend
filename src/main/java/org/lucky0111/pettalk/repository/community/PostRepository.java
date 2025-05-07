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
    Page<Post> findAll(Specification<Post> postSpecification, Pageable pageable);

    List<Post> findByUser_UserId(UUID userUserId);
}
