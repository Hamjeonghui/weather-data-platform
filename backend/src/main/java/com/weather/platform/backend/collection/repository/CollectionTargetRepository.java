package com.weather.platform.backend.collection.repository;

import com.weather.platform.backend.collection.entity.CollectionTarget;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionTargetRepository extends JpaRepository<CollectionTarget, Long> {

    List<CollectionTarget> findByEnabledTrue();

    long countByEnabledTrue();
}
