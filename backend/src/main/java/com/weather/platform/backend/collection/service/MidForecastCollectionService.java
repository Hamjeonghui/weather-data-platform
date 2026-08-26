package com.weather.platform.backend.collection.service;

import com.weather.platform.backend.collection.client.KmaMidForecastClient;
import com.weather.platform.backend.collection.entity.CollectionJob;
import com.weather.platform.backend.collection.entity.CollectionTarget;
import com.weather.platform.backend.forecast.entity.ForecastPeriod;
import com.weather.platform.backend.forecast.repository.MidForecastRepository;
import com.weather.platform.backend.location.entity.LocationInfo;
import com.weather.platform.backend.location.repository.LocationInfoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MidForecastCollectionService implements CollectionExecutor {

    private static final Logger log = LoggerFactory.getLogger(MidForecastCollectionService.class);

    private static final ZoneOffset SEOUL_OFFSET = ZoneOffset.of("+09:00");
    private static final DateTimeFormatter TM_FC_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final int[] AM_PM_DAYS = {3, 4, 5, 6, 7};
    private static final int[] DAY_ONLY_DAYS = {8, 9, 10};

    private final LocationInfoRepository locationInfoRepository;
    private final MidForecastRepository midForecastRepository;
    private final KmaMidForecastClient kmaMidForecastClient;

    public MidForecastCollectionService(LocationInfoRepository locationInfoRepository,
                                         MidForecastRepository midForecastRepository,
                                         KmaMidForecastClient kmaMidForecastClient) {
        this.locationInfoRepository = locationInfoRepository;
        this.midForecastRepository = midForecastRepository;
        this.kmaMidForecastClient = kmaMidForecastClient;
    }

    @Override
    public String supportedDataCode() {
        return "MID_FORECAST";
    }

    @Override
    public boolean collect(CollectionTarget target, CollectionJob job) {
        Long targetId = target.getTargetId();
        OffsetDateTime baseAt = calculateLatestAnnouncementTime(OffsetDateTime.now(SEOUL_OFFSET));
        String tmFc = baseAt.format(TM_FC_FORMATTER);

        List<LocationInfo> locations = locationInfoRepository.findAll();
        boolean anySuccess = false;

        for (LocationInfo location : locations) {
            try {
                Map<String, Object> item = kmaMidForecastClient.fetchMidLandFcst(location.getRegId(), tmFc);
                saveForecastRows(targetId, job.getJobId(), location.getRegId(), baseAt, item);
                anySuccess = true;
            } catch (Exception e) {
                log.warn("중기예보 수집 실패: regId={}", location.getRegId(), e);
            }
        }

        return anySuccess;
    }

    private void saveForecastRows(Long targetId, Long jobId, String stnId, OffsetDateTime baseAt, Map<String, Object> item) {
        LocalDate baseDate = baseAt.toLocalDate();

        for (int day : AM_PM_DAYS) {
            saveIfPresent(targetId, jobId, stnId, baseAt, forecastAt(baseDate, day, ForecastPeriod.AM), ForecastPeriod.AM,
                    item, "rnSt" + day + "Am", "wf" + day + "Am");
            saveIfPresent(targetId, jobId, stnId, baseAt, forecastAt(baseDate, day, ForecastPeriod.PM), ForecastPeriod.PM,
                    item, "rnSt" + day + "Pm", "wf" + day + "Pm");
        }
        for (int day : DAY_ONLY_DAYS) {
            saveIfPresent(targetId, jobId, stnId, baseAt, forecastAt(baseDate, day, ForecastPeriod.DAY), ForecastPeriod.DAY,
                    item, "rnSt" + day, "wf" + day);
        }
    }

    private void saveIfPresent(Long targetId, Long jobId, String stnId, OffsetDateTime baseAt, OffsetDateTime fcstAt,
                                ForecastPeriod period, Map<String, Object> item, String rnStKey, String wfKey) {
        if (!item.containsKey(rnStKey) && !item.containsKey(wfKey)) {
            return;
        }
        BigDecimal rnSt = toBigDecimal(item.get(rnStKey));
        Object wfValue = item.get(wfKey);
        String wf = wfValue == null ? null : wfValue.toString();
        midForecastRepository.upsert(jobId, targetId, stnId, baseAt, fcstAt, rnSt, wf, period.name());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        return new BigDecimal(value.toString());
    }

    private OffsetDateTime forecastAt(LocalDate baseDate, int day, ForecastPeriod period) {
        LocalDate targetDate = baseDate.plusDays(day);
        LocalTime time = period == ForecastPeriod.PM ? LocalTime.NOON : LocalTime.MIDNIGHT;
        return OffsetDateTime.of(targetDate, time, SEOUL_OFFSET);
    }

    private OffsetDateTime calculateLatestAnnouncementTime(OffsetDateTime now) {
        OffsetDateTime today0600 = now.withHour(6).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime today1800 = now.withHour(18).withMinute(0).withSecond(0).withNano(0);

        if (!now.isBefore(today1800)) {
            return today1800;
        }
        if (!now.isBefore(today0600)) {
            return today0600;
        }
        return today1800.minusDays(1);
    }
}
