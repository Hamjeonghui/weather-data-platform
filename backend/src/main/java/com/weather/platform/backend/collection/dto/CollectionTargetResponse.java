package com.weather.platform.backend.collection.dto;

import java.time.OffsetDateTime;

public record CollectionTargetResponse(Long id,
                                        String dataCode,
                                        String dataNameKo,
                                        boolean enabled,
                                        String scheduleType,
                                        int intervalValue,
                                        OffsetDateTime lastExecutedAt,
                                        OffsetDateTime nextExecutedAt,
                                        String latestStatus) {
}
