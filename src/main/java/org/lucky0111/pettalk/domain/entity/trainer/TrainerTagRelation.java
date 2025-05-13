package org.lucky0111.pettalk.domain.entity.trainer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.lucky0111.pettalk.domain.entity.common.Tag;

import java.util.Objects;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", foreignKey = @ForeignKey(name = "FK_TAG_TRAINER",
            foreignKeyDefinition = "FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE"))
    private Tag tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainerTagRelation that = (TrainerTagRelation) o;

        return Objects.equals(trainer, that.trainer) &&
                Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainer, tag);
    }
}
