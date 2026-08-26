package com.weather.platform.backend.collection.service;

import com.weather.platform.backend.collection.client.KmaMidForecastClient;
import com.weather.platform.backend.collection.dto.ExecuteCollectionResponse;
import com.weather.platform.backend.collection.entity.CollectionJob;
import com.weather.platform.backend.collection.entity.CollectionStatus;
import com.weather.platform.backend.collection.entity.TriggerType;
import com.weather.platform.backend.collection.repository.CollectionJobRepository;
import com.weather.platform.backend.collection.repository.CollectionTargetRepository;
import com.weather.platform.backend.forecast.entity.ForecastPeriod;
import com.weather.platform.backend.forecast.repository.MidForecastRepository;
import com.weather.platform.backend.global.exception.BusinessException;
import com.weather.platform.backend.global.exception.ErrorCode;
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
import org.springframework.transaction.annotation.Transactional;

@Service
public class MidForecastCollectionService {

    private static final Logger log = LoggerFactory.getLogger(MidForecastCollectionService.class);

    private static final ZoneOffset SEOUL_OFFSET = ZoneOffset.of("+09:00");
    private static final DateTimeFormatter TM_FC_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final int[] AM_PM_DAYS = {3, 4, 5, 6, 7};
    private static final int[] DAY_ONLY_DAYS = {8, 9, 10};

    private final CollectionTargetRepository collectionTargetRepository;
    private final CollectionJobRepository collectionJobRepository;
    private final LocationInfoRepository locationInfoRepository;
    private final MidForecastRepository midForecastRepository;
    private final KmaMidForecastClient kmaMidForecastClient;

    public MidForecastCollectionService(CollectionTargetRepository collectionTargetRepository,
                                         CollectionJobRepository collectionJobRepository,
                                         LocationInfoRepository locationInfoRepository,
                                         MidForecastRepository midForecastRepository,
                                         KmaMidForecastClient kmaMidForecastClient) {
        this.collectionTargetRepository = collectionTargetRepository;
        this.collectionJobRepository = collectionJobRepository;
        this.locationInfoRepository = locationInfoRepository;
        this.midForecastRepository = midForecastRepository;
        this.kmaMidForecastClient = kmaMidForecastClient;
    }

    @Transactional
    public ExecuteCollectionResponse execute(Long targetId, String executedBy) {
        collectionTargetRepository.findById(targetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_TARGET_NOT_FOUND));

        if (collectionJobRepository.existsByTargetIdAndStatus(targetId, CollectionStatus.RUNNING)) {
            throw new BusinessException(ErrorCode.COLLECTION_ALREADY_RUNNING);
        }

        OffsetDateTime baseAt = calculateLatestAnnouncementTime(OffsetDateTime.now(SEOUL_OFFSET));
        String tmFc = baseAt.format(TM_FC_FORMATTER);

        CollectionJob job = collectionJobRepository.save(
                new CollectionJob(targetId, CollectionStatus.RUNNING, TriggerType.MANUAL,
                        OffsetDateTime.now(SEOUL_OFFSET), executedBy));

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

        CollectionStatus finalStatus = anySuccess ? CollectionStatus.SUCCESS : CollectionStatus.FAILED;
        job.complete(finalStatus, OffsetDateTime.now(SEOUL_OFFSET),
                anySuccess ? null : ErrorCode.EXTERNAL_API_ERROR.name());

        return new ExecuteCollectionResponse(job.getJobId(), job.getStatus().name());
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
