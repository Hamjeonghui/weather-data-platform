package com.weather.platform.backend.collection.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCollectionTargetRequest(
        @NotNull(message = "활성화 여부는 필수입니다.") Boolean enabled,
        @NotNull(message = "수행 단위는 필수입니다.") String scheduleType,
        @NotNull(message = "수행 시간은 필수입니다.") Integer intervalValue,
        String executionTime
) {
}
