package com.weather.platform.backend.collection.dto;

import java.time.OffsetDateTime;

public record CollectionDashboardSummaryResponse(long targetCount,
                                                   long enabledTargetCount,
                                                   long runningCount,
                                                   long successCount,
                                                   long failedCount,
                                                   OffsetDateTime latestCollectedAt) {
}
