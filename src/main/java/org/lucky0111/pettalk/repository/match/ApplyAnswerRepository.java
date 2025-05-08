package org.lucky0111.pettalk.repository.match;

import org.lucky0111.pettalk.domain.entity.match.ApplyAnswer;
import org.lucky0111.pettalk.domain.entity.match.UserApply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplyAnswerRepository extends JpaRepository<ApplyAnswer, Long> {
    Optional<ApplyAnswer> findByUserApply(UserApply userApply);
}
