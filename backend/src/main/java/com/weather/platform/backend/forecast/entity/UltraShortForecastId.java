package com.weather.platform.backend.forecast.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

public class UltraShortForecastId implements Serializable {

    private Long targetId;
    private String stnId;
    private OffsetDateTime baseAt;
    private OffsetDateTime fcstAt;

    public UltraShortForecastId() {
    }

    public UltraShortForecastId(Long targetId, String stnId, OffsetDateTime baseAt, OffsetDateTime fcstAt) {
        this.targetId = targetId;
        this.stnId = stnId;
        this.baseAt = baseAt;
        this.fcstAt = fcstAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UltraShortForecastId that)) {
            return false;
        }
        return Objects.equals(targetId, that.targetId)
                && Objects.equals(stnId, that.stnId)
                && Objects.equals(baseAt, that.baseAt)
                && Objects.equals(fcstAt, that.fcstAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetId, stnId, baseAt, fcstAt);
    }
}
