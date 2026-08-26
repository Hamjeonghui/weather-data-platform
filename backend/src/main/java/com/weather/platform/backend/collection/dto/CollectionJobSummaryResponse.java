package com.weather.platform.backend.collection.dto;

import java.time.OffsetDateTime;

public record CollectionJobSummaryResponse(Long id,
                                            Long targetId,
                                            String targetName,
                                            String status,
                                            String triggerType,
                                            OffsetDateTime startedAt,
                                            OffsetDateTime finishedAt,
                                            Long receivedCount,
                                            Long savedCount,
                                            Long duplicateCount,
                                            String errorCode) {
}
