package org.lucky0111.pettalk.repository.community;

import org.lucky0111.pettalk.domain.entity.community.Post;
import org.lucky0111.pettalk.domain.entity.community.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    void deleteByPost(Post post);

    void deleteByImageUrl(String url);

    List<PostImage> findByPost(Post post);
}