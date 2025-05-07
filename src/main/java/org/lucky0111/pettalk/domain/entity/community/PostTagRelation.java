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
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;
}
