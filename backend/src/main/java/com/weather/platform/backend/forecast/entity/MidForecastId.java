package com.weather.platform.backend.forecast.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

public class MidForecastId implements Serializable {

    private Long targetId;
    private String stnId;
    private OffsetDateTime baseAt;
    private OffsetDateTime fcstAt;
    private ForecastPeriod period;

    public MidForecastId() {
    }

    public MidForecastId(Long targetId, String stnId, OffsetDateTime baseAt, OffsetDateTime fcstAt, ForecastPeriod period) {
        this.targetId = targetId;
        this.stnId = stnId;
        this.baseAt = baseAt;
        this.fcstAt = fcstAt;
        this.period = period;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MidForecastId that)) {
            return false;
        }
        return Objects.equals(targetId, that.targetId)
                && Objects.equals(stnId, that.stnId)
                && Objects.equals(baseAt, that.baseAt)
                && Objects.equals(fcstAt, that.fcstAt)
                && period == that.period;
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetId, stnId, baseAt, fcstAt, period);
    }
}
