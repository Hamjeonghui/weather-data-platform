package com.weather.platform.backend.forecast.repository;

import com.weather.platform.backend.forecast.entity.ShortForecast;
import com.weather.platform.backend.forecast.entity.ShortForecastId;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShortForecastRepository extends JpaRepository<ShortForecast, ShortForecastId> {

    @Modifying
    @Query(value = """
            INSERT INTO short_forecast (job_id, target_id, stn_id, base_at, fcst_at, pop, rn, reh, tmp, tmn, tmx, wd, ws)
            VALUES (:jobId, :targetId, :stnId, :baseAt, :fcstAt, :pop, :rn, :reh, :tmp, :tmn, :tmx, :wd, :ws)
            ON CONFLICT (target_id, stn_id, base_at, fcst_at)
            DO UPDATE SET job_id = EXCLUDED.job_id, pop = EXCLUDED.pop, rn = EXCLUDED.rn, reh = EXCLUDED.reh,
                          tmp = EXCLUDED.tmp, tmn = EXCLUDED.tmn, tmx = EXCLUDED.tmx, wd = EXCLUDED.wd, ws = EXCLUDED.ws
            """, nativeQuery = true)
    void upsert(@Param("jobId") Long jobId,
                @Param("targetId") Long targetId,
                @Param("stnId") String stnId,
                @Param("baseAt") OffsetDateTime baseAt,
                @Param("fcstAt") OffsetDateTime fcstAt,
                @Param("pop") BigDecimal pop,
                @Param("rn") BigDecimal rn,
                @Param("reh") BigDecimal reh,
                @Param("tmp") BigDecimal tmp,
                @Param("tmn") BigDecimal tmn,
                @Param("tmx") BigDecimal tmx,
                @Param("wd") BigDecimal wd,
                @Param("ws") BigDecimal ws);
}
