package org.lucky0111.pettalk.domain.entity.common;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tagId;

    @Column(unique = true)
    private String tagName;
}