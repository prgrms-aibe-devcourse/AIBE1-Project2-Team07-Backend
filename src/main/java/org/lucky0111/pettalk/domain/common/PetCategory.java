package org.lucky0111.pettalk.domain.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PetCategory {
    DOG,
    CAT,
    ETC;
}
