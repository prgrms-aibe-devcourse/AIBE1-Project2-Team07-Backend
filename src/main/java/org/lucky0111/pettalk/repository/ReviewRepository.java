package org.lucky0111.pettalk.repository;

import org.lucky0111.pettalk.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 특정 Trainer의 평균 평점을 조회하는 쿼리 (관계 필드 조인 사용)
    // Review(r) 엔티티의 userApply 관계 필드를 타고 UserApply(ua) 엔티티 조인
    // UserApply(ua) 엔티티의 trainer 관계 필드를 타고 Trainer(t) 엔티티 조인 ( Trainer 엔티티의 PK 필드는 trainerId 임)
    @Query("SELECT AVG(r.rating) FROM Review r JOIN r.userApply ua JOIN ua.trainer t WHERE t.trainerId = :trainerId")
    Double findAverageRatingByTrainerId(@Param("trainerId") UUID trainerId);

    // 특정 Trainer의 후기(Review) 개수를 조회하는 쿼리 (관계 필드 조인 사용)
    // 위와 동일하게 관계 필드를 타고 조인
    @Query("SELECT COUNT(r) FROM Review r JOIN r.userApply ua JOIN ua.trainer t WHERE t.trainerId = :trainerId")
    Long countByReviewedTrainerId(@Param("trainerId") UUID trainerId);

}
