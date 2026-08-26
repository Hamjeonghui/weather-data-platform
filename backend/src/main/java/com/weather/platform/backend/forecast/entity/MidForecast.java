package com.weather.platform.backend.forecast.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "mid_forecast")
@IdClass(MidForecastId.class)
public class MidForecast {

    @Id
    @Column(name = "target_id")
    private Long targetId;

    @Id
    @Column(name = "stn_id")
    private String stnId;

    @Id
    @Column(name = "base_at")
    private OffsetDateTime baseAt;

    @Id
    @Column(name = "fcst_at")
    private OffsetDateTime fcstAt;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "period")
    private ForecastPeriod period;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "rn_st")
    private BigDecimal rnSt;

    @Column(name = "wf")
    private String wf;

    protected MidForecast() {
    }

    public MidForecast(Long targetId, String stnId, OffsetDateTime baseAt, OffsetDateTime fcstAt,
                        ForecastPeriod period, Long jobId, BigDecimal rnSt, String wf) {
        this.targetId = targetId;
        this.stnId = stnId;
        this.baseAt = baseAt;
        this.fcstAt = fcstAt;
        this.period = period;
        this.jobId = jobId;
        this.rnSt = rnSt;
        this.wf = wf;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getStnId() {
        return stnId;
    }

    public OffsetDateTime getBaseAt() {
        return baseAt;
    }

    public OffsetDateTime getFcstAt() {
        return fcstAt;
    }

    public ForecastPeriod getPeriod() {
        return period;
    }

    public Long getJobId() {
        return jobId;
    }

    public BigDecimal getRnSt() {
        return rnSt;
    }

    public String getWf() {
        return wf;
    }
}
