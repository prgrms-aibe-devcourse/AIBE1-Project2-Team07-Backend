package org.lucky0111.pettalk.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "trainer_tags", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tag_id", "trainer_id"})
})
public class TrainerTag {
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
