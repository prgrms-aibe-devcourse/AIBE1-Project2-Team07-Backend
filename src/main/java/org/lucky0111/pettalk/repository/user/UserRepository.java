package org.lucky0111.pettalk.repository.user;

import org.lucky0111.pettalk.domain.entity.PetUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<PetUser, String> {

    PetUser findByProviderAndSocialId(String provider, String socialId);

    boolean existsByNickname(String nickname);
}