package com.weather.platform.backend.collection.service;

import java.time.Duration;

public final class ScheduleIntervalCalculator {

    private ScheduleIntervalCalculator() {
    }

    public static Duration toDuration(String scheduleType, int intervalValue) {
        return switch (scheduleType) {
            case "MINUTE" -> Duration.ofMinutes(intervalValue);
            case "HOUR" -> Duration.ofHours(intervalValue);
            case "DAY" -> Duration.ofDays(intervalValue);
            default -> throw new IllegalStateException("지원하지 않는 schedule_type입니다: " + scheduleType);
        };
    }
}
