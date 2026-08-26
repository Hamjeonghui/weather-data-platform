package com.weather.platform.backend.forecast.repository;

import com.weather.platform.backend.forecast.entity.MidForecast;
import com.weather.platform.backend.forecast.entity.MidForecastId;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MidForecastRepository extends JpaRepository<MidForecast, MidForecastId> {

    @Modifying
    @Query(value = """
            INSERT INTO mid_forecast (job_id, target_id, stn_id, base_at, fcst_at, rn_st, wf, period)
            VALUES (:jobId, :targetId, :stnId, :baseAt, :fcstAt, :rnSt, :wf, :period)
            ON CONFLICT (target_id, stn_id, base_at, fcst_at, period)
            DO UPDATE SET job_id = EXCLUDED.job_id, rn_st = EXCLUDED.rn_st, wf = EXCLUDED.wf
            """, nativeQuery = true)
    void upsert(@Param("jobId") Long jobId,
                @Param("targetId") Long targetId,
                @Param("stnId") String stnId,
                @Param("baseAt") OffsetDateTime baseAt,
                @Param("fcstAt") OffsetDateTime fcstAt,
                @Param("rnSt") BigDecimal rnSt,
                @Param("wf") String wf,
                @Param("period") String period);
}
