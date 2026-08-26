package com.weather.platform.backend.collection.repository;

import com.weather.platform.backend.collection.entity.CollectionJob;
import com.weather.platform.backend.collection.entity.CollectionStatus;
import com.weather.platform.backend.collection.entity.TriggerType;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CollectionJobRepository extends JpaRepository<CollectionJob, Long> {

    boolean existsByTargetIdAndStatus(Long targetId, CollectionStatus status);

    Optional<CollectionJob> findFirstByTargetIdOrderByStartedAtDesc(Long targetId);

    long countByStatus(CollectionStatus status);

    long countByStatusAndStartedAtBetween(CollectionStatus status, OffsetDateTime from, OffsetDateTime to);

    @Query("SELECT MAX(j.finishedAt) FROM CollectionJob j "
            + "WHERE j.status = :status AND j.startedAt BETWEEN :from AND :to")
    Optional<OffsetDateTime> findLatestFinishedAt(@Param("status") CollectionStatus status,
                                                   @Param("from") OffsetDateTime from,
                                                   @Param("to") OffsetDateTime to);

    @Query("SELECT j FROM CollectionJob j "
            + "WHERE (:targetId IS NULL OR j.targetId = :targetId) "
            + "AND (:status IS NULL OR j.status = :status) "
            + "AND (:triggerType IS NULL OR j.triggerType = :triggerType) "
            + "AND j.startedAt BETWEEN :from AND :to")
    Page<CollectionJob> search(@Param("targetId") Long targetId,
                                @Param("status") CollectionStatus status,
                                @Param("triggerType") TriggerType triggerType,
                                @Param("from") OffsetDateTime from,
                                @Param("to") OffsetDateTime to,
                                Pageable pageable);
}
