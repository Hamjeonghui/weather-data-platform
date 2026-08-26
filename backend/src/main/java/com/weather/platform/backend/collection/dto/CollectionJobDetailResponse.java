package com.weather.platform.backend.collection.dto;

import java.time.OffsetDateTime;

public record CollectionJobDetailResponse(Long id,
                                           String targetName,
                                           String status,
                                           String triggerType,
                                           OffsetDateTime startedAt,
                                           OffsetDateTime finishedAt,
                                           Long receivedCount,
                                           Long savedCount,
                                           Long duplicateCount,
                                           String errorCode,
                                           String errorMessage,
                                           Boolean retryable,
                                           Long retryOfJobId) {
}
