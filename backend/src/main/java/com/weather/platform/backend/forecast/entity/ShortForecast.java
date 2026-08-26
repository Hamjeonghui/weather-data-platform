package com.weather.platform.backend.forecast.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "short_forecast")
@IdClass(ShortForecastId.class)
public class ShortForecast {

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

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "pop")
    private BigDecimal pop;

    @Column(name = "rn")
    private BigDecimal rn;

    @Column(name = "reh")
    private BigDecimal reh;

    @Column(name = "tmp")
    private BigDecimal tmp;

    @Column(name = "tmn")
    private BigDecimal tmn;

    @Column(name = "tmx")
    private BigDecimal tmx;

    @Column(name = "wd")
    private BigDecimal wd;

    @Column(name = "ws")
    private BigDecimal ws;

    protected ShortForecast() {
    }

    public ShortForecast(Long targetId, String stnId, OffsetDateTime baseAt, OffsetDateTime fcstAt, Long jobId,
                          BigDecimal pop, BigDecimal rn, BigDecimal reh, BigDecimal tmp, BigDecimal tmn,
                          BigDecimal tmx, BigDecimal wd, BigDecimal ws) {
        this.targetId = targetId;
        this.stnId = stnId;
        this.baseAt = baseAt;
        this.fcstAt = fcstAt;
        this.jobId = jobId;
        this.pop = pop;
        this.rn = rn;
        this.reh = reh;
        this.tmp = tmp;
        this.tmn = tmn;
        this.tmx = tmx;
        this.wd = wd;
        this.ws = ws;
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

    public Long getJobId() {
        return jobId;
    }

    public BigDecimal getPop() {
        return pop;
    }

    public BigDecimal getRn() {
        return rn;
    }

    public BigDecimal getReh() {
        return reh;
    }

    public BigDecimal getTmp() {
        return tmp;
    }

    public BigDecimal getTmn() {
        return tmn;
    }

    public BigDecimal getTmx() {
        return tmx;
    }

    public BigDecimal getWd() {
        return wd;
    }

    public BigDecimal getWs() {
        return ws;
    }
}
