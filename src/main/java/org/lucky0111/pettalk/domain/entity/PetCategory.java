package org.lucky0111.pettalk.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "pet_categories")
public class PetCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long petCategoryId;

    @Column(unique = true)
    private String petCategoryName;
}
