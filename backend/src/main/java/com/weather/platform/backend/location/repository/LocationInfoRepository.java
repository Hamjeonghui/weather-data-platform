package com.weather.platform.backend.location.repository;

import com.weather.platform.backend.location.entity.LocationInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationInfoRepository extends JpaRepository<LocationInfo, String> {
}
