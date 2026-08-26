package com.weather.platform.backend.collection.repository;

import com.weather.platform.backend.collection.entity.CollectionError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionErrorRepository extends JpaRepository<CollectionError, String> {
}
