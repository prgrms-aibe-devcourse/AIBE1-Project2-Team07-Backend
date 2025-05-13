package org.lucky0111.pettalk.repository.trainer;

import org.lucky0111.pettalk.domain.entity.trainer.TrainerTagRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TrainerTagRepository extends JpaRepository<TrainerTagRelation, Long> {
    List<TrainerTagRelation> findByTrainer_TrainerId(UUID trainerId);

    List<TrainerTagRelation> findAllByTrainer_TrainerIdIn(Collection<UUID> trainerIds);

    /**
     * 태그 ID와 트레이너 ID로 관계를 중복 없이 삽입합니다.
     * 이미 동일한 태그 ID와 트레이너 ID 조합이 존재하면 삽입을 무시합니다.
     *
     * @param trainerId 트레이너 ID
     * @param tagId 태그 ID
     * @return 영향받은 행 수
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO trainer_tags (trainer_id, tag_id) VALUES (:trainerId, :tagId)",
            nativeQuery = true)
    int insertIgnore(@Param("trainerId") UUID trainerId, @Param("tagId") Long tagId);

    /**
     * 태그 ID 목록과 트레이너 ID로 여러 관계를 중복 없이 삽입합니다.
     * 이미 존재하는 조합은 무시합니다.
     *
     * @param trainerId 트레이너 ID
     * @param tagIds 태그 ID 목록
     */
    @Transactional
    default void insertIgnoreBatch(@Param("trainerId") UUID trainerId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            insertIgnore(trainerId, tagId);
        }
    }

    /**
     * 트레이너 ID에 해당하는 모든 태그 관계를 삭제합니다.
     *
     * @param trainerId 트레이너 ID
     * @return 영향받은 행 수
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM trainer_tags WHERE trainer_id = :trainerId", nativeQuery = true)
    int deleteAllByTrainerId(@Param("trainerId") UUID trainerId);

    /**
     * 트레이너의 태그를 업데이트합니다. 기존 태그를 모두 삭제하고 새로운 태그를 추가합니다.
     *
     * @param trainerId 트레이너 ID
     * @param tagIds 새로운 태그 ID 목록
     */
    @Transactional
    default void updateTrainerTags(UUID trainerId, List<Long> tagIds) {
        deleteAllByTrainerId(trainerId);
        insertIgnoreBatch(trainerId, tagIds);
    }
}
