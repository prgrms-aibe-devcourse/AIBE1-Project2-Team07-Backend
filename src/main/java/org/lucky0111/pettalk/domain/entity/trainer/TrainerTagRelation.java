package org.lucky0111.pettalk.domain.entity.trainer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.lucky0111.pettalk.domain.entity.common.Tag;

@Getter
@Setter
@Entity
@Table(name = "trainer_tags", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tag_id", "trainer_id"})
})
public class TrainerTagRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;
}
