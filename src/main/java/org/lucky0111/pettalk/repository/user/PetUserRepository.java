package org.lucky0111.pettalk.repository.user;

import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PetUserRepository extends JpaRepository<PetUser, UUID> {
    Optional<PetUser> findBySocialId(String socialId);

    boolean existsByNickname(String nickname);
}
