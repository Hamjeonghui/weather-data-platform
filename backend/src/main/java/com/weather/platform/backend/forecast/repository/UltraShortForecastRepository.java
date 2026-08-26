package com.weather.platform.backend.forecast.repository;

import com.weather.platform.backend.forecast.entity.UltraShortForecast;
import com.weather.platform.backend.forecast.entity.UltraShortForecastId;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UltraShortForecastRepository extends JpaRepository<UltraShortForecast, UltraShortForecastId> {

    /**
     * @return true면 신규 삽입, false면 기존 행 갱신(중복 재수집)
     */
    @Query(value = """
            INSERT INTO ultra_short_forecast (job_id, target_id, stn_id, base_at, fcst_at, pop, rn, reh, tmp, wd, ws, lgt)
            VALUES (:jobId, :targetId, :stnId, :baseAt, :fcstAt, :pop, :rn, :reh, :tmp, :wd, :ws, :lgt)
            ON CONFLICT (target_id, stn_id, base_at, fcst_at)
            DO UPDATE SET job_id = EXCLUDED.job_id, pop = EXCLUDED.pop, rn = EXCLUDED.rn, reh = EXCLUDED.reh,
                          tmp = EXCLUDED.tmp, wd = EXCLUDED.wd, ws = EXCLUDED.ws, lgt = EXCLUDED.lgt
            RETURNING (xmax = 0)
            """, nativeQuery = true)
    boolean upsert(@Param("jobId") Long jobId,
                    @Param("targetId") Long targetId,
                    @Param("stnId") String stnId,
                    @Param("baseAt") OffsetDateTime baseAt,
                    @Param("fcstAt") OffsetDateTime fcstAt,
                    @Param("pop") BigDecimal pop,
                    @Param("rn") BigDecimal rn,
                    @Param("reh") BigDecimal reh,
                    @Param("tmp") BigDecimal tmp,
                    @Param("wd") BigDecimal wd,
                    @Param("ws") BigDecimal ws,
                    @Param("lgt") BigDecimal lgt);
}
