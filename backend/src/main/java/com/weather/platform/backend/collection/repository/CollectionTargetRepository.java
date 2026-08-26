package com.weather.platform.backend.collection.repository;

import com.weather.platform.backend.collection.entity.CollectionTarget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionTargetRepository extends JpaRepository<CollectionTarget, Long> {
}
