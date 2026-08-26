package com.weather.platform.backend.collection.repository;

import com.weather.platform.backend.collection.entity.CollectionJob;
import com.weather.platform.backend.collection.entity.CollectionStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionJobRepository extends JpaRepository<CollectionJob, Long> {

    boolean existsByTargetIdAndStatus(Long targetId, CollectionStatus status);

    Optional<CollectionJob> findFirstByTargetIdOrderByStartedAtDesc(Long targetId);
}
