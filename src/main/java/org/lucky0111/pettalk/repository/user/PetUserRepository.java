package org.lucky0111.pettalk.repository.user;

import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PetUserRepository extends JpaRepository<PetUser, UUID> {
    Optional<PetUser> findBySocialId(String socialId);

    boolean existsByNickname(String nickname);
}
