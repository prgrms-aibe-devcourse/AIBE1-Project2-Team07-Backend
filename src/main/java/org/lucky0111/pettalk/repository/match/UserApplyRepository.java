package org.lucky0111.pettalk.repository.match;

import org.lucky0111.pettalk.domain.common.Status;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.domain.entity.trainer.Trainer;
import org.lucky0111.pettalk.domain.entity.match.UserApply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public interface UserApplyRepository extends JpaRepository<UserApply, Long> {
    boolean existsByPetUser_userIdAndTrainer_trainerIdAndStatus(UUID petUserId, UUID trainerId, Status status);

    List<UserApply> findByTrainer_TrainerId(UUID trainerTrainerId);

    /**
     * 신청 ID로 관련 엔티티를 함께 조회
     * @param applyId 신청 ID
     * @return 신청서 Optional 객체
     */
    @Query("SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE ua.applyId = :applyId")
    Optional<UserApply> findByIdWithRelations(@Param("applyId") Long applyId);

    /**
     * 사용자 ID로 해당 사용자의 모든 신청서를 관련 엔티티와 함께 조회
     * @param userId 사용자 ID
     * @return 신청서 목록
     */
    @Query("SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE ua.petUser.userId = :userId " +
            "ORDER BY ua.createdAt DESC")
    List<UserApply> findByPetUser_UserIdWithRelations(@Param("userId") UUID userId);

    /**
     * 사용자 ID로 해당 사용자의 모든 신청서를 상태별로 필터링하여 관련 엔티티와 함께 조회
     * @param userId 사용자 ID
     * @param status 신청 상태
     * @return 신청서 목록
     */
    @Query("SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE ua.petUser.userId = :userId " +
            "AND ua.status = :status " +
            "ORDER BY ua.createdAt DESC")
    List<UserApply> findByPetUser_UserIdAndStatusWithRelations(
            @Param("userId") UUID userId,
            @Param("status") Status status);

    /**
     * 트레이너 ID로 해당 트레이너에게 온 모든 신청서를 관련 엔티티와 함께 조회
     * @param trainerId 트레이너 ID
     * @return 신청서 목록
     */
    @Query("SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE t.trainerId = :trainerId " +
            "ORDER BY ua.createdAt DESC")
    List<UserApply> findByTrainer_TrainerIdWithRelations(@Param("trainerId") UUID trainerId);

    /**
     * 트레이너 ID로 해당 트레이너에게 온 모든 신청서를 상태별로 필터링하여 관련 엔티티와 함께 조회
     * @param trainerId 트레이너 ID
     * @param status 신청 상태
     * @return 신청서 목록
     */
    @Query("SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE t.trainerId = :trainerId " +
            "AND ua.status = :status " +
            "ORDER BY ua.createdAt DESC")
    List<UserApply> findByTrainer_TrainerIdAndStatusWithRelations(
            @Param("trainerId") UUID trainerId,
            @Param("status") Status status);

    /**
     * 사용자 ID로 해당 사용자의 모든 신청서를 페이징 처리하여 관련 엔티티와 함께 조회
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 신청서 목록
     */
    @Query(value = "SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE ua.petUser.userId = :userId",
            countQuery = "SELECT COUNT(ua) FROM UserApply ua WHERE ua.petUser.userId = :userId")
    Page<UserApply> findByPetUser_UserIdWithRelationsPaged(
            @Param("userId") UUID userId,
            Pageable pageable);

    /**
     * 트레이너 ID로 해당 트레이너에게 온 모든 신청서를 페이징 처리하여 관련 엔티티와 함께 조회
     * @param trainerId 트레이너 ID
     * @param pageable 페이징 정보
     * @return 페이징된 신청서 목록
     */
    @Query(value = "SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE t.trainerId = :trainerId",
            countQuery = "SELECT COUNT(ua) FROM UserApply ua JOIN ua.trainer t WHERE t.trainerId = :trainerId")
    Page<UserApply> findByTrainer_TrainerIdWithRelationsPaged(
            @Param("trainerId") UUID trainerId,
            Pageable pageable);

    /**
     * 신청서 ID 목록을 기반으로 리뷰 존재 여부 확인
     * @param applyIds 신청서 ID 목록
     * @return 리뷰 존재 여부 (Key: 신청서 ID, Value: 리뷰 존재 여부)
     */
    @Query("SELECT ua.applyId, " +
            "(SELECT COUNT(r) FROM Review r WHERE r.userApply.applyId = ua.applyId) > 0 " +
            "FROM UserApply ua " +
            "WHERE ua.applyId IN :applyIds")
    List<Object[]> checkReviewExistenceByApplyIds(@Param("applyIds") List<Long> applyIds);

    /**
     * 사용자 ID와 상태로 해당 사용자의 상태별 신청서를 페이징 처리하여 관련 엔티티와 함께 조회
     * @param userId 사용자 ID
     * @param status 신청 상태
     * @param pageable 페이징 정보
     * @return 페이징된 신청서 목록
     */
    @Query(value = "SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE ua.petUser.userId = :userId " +
            "AND ua.status = :status",
            countQuery = "SELECT COUNT(ua) FROM UserApply ua " +
                    "WHERE ua.petUser.userId = :userId AND ua.status = :status")
    Page<UserApply> findByPetUser_UserIdAndStatusWithRelationsPaged(
            @Param("userId") UUID userId,
            @Param("status") Status status,
            Pageable pageable);

    /**
     * 트레이너 ID와 상태로 해당 트레이너에게 온 상태별 신청서를 페이징 처리하여 관련 엔티티와 함께 조회
     * @param trainerId 트레이너 ID
     * @param status 신청 상태
     * @param pageable 페이징 정보
     * @return 페이징된 신청서 목록
     */
    @Query(value = "SELECT ua FROM UserApply ua " +
            "JOIN FETCH ua.petUser " +
            "JOIN FETCH ua.trainer t " +
            "JOIN FETCH t.user " +
            "WHERE t.trainerId = :trainerId " +
            "AND ua.status = :status",
            countQuery = "SELECT COUNT(ua) FROM UserApply ua " +
                    "JOIN ua.trainer t WHERE t.trainerId = :trainerId AND ua.status = :status")
    Page<UserApply> findByTrainer_TrainerIdAndStatusWithRelationsPaged(
            @Param("trainerId") UUID trainerId,
            @Param("status") Status status,
            Pageable pageable);

    /**
     * 신청서 ID 목록을 기반으로 리뷰 존재 여부를 맵으로 변환
     * @param applyIds 신청서 ID 목록
     * @return 맵(키: 신청서 ID, 값: 리뷰 존재 여부)
     */
    default Map<Long, Boolean> hasReviewByApplyIds(List<Long> applyIds) {
        return checkReviewExistenceByApplyIds(applyIds).stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Boolean) arr[1]
                ));
    }


}
