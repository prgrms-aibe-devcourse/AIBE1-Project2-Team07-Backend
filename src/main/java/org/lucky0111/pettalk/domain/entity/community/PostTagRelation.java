package org.lucky0111.pettalk.domain.entity.community;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.lucky0111.pettalk.domain.entity.common.Tag;

@Setter
@Getter
@Entity
@Table(name = "post_tags", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tag_id", "post_id"})
})
public class PostTagRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tag_id", foreignKey = @ForeignKey(name = "FK_POST_TAG",
            foreignKeyDefinition = "FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE"))
    private Tag tag;

    @ManyToOne
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name = "FK_POST_TAGS",
            foreignKeyDefinition = "FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE"))
    private Post post;
}
