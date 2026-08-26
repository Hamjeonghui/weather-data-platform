package com.weather.platform.backend.collection.service;

import com.weather.platform.backend.collection.client.KmaVilageFcstClient;
import com.weather.platform.backend.collection.entity.CollectionJob;
import com.weather.platform.backend.collection.entity.CollectionTarget;
import com.weather.platform.backend.forecast.repository.UltraShortForecastRepository;
import com.weather.platform.backend.location.entity.LocationInfo;
import com.weather.platform.backend.location.repository.LocationInfoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UltraShortForecastCollectionService implements CollectionExecutor {

    private static final Logger log = LoggerFactory.getLogger(UltraShortForecastCollectionService.class);

    private static final ZoneOffset SEOUL_OFFSET = ZoneOffset.of("+09:00");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");
    private static final Set<String> SUPPORTED_CATEGORIES =
            Set.of("POP", "RN1", "REH", "T1H", "VEC", "WSD", "LGT");

    private final LocationInfoRepository locationInfoRepository;
    private final UltraShortForecastRepository ultraShortForecastRepository;
    private final KmaVilageFcstClient kmaVilageFcstClient;
    private final String ultraShortForecastUrl;

    public UltraShortForecastCollectionService(LocationInfoRepository locationInfoRepository,
                                                UltraShortForecastRepository ultraShortForecastRepository,
                                                KmaVilageFcstClient kmaVilageFcstClient,
                                                @Value("${kma.ultra-short-forecast-url}") String ultraShortForecastUrl) {
        this.locationInfoRepository = locationInfoRepository;
        this.ultraShortForecastRepository = ultraShortForecastRepository;
        this.kmaVilageFcstClient = kmaVilageFcstClient;
        this.ultraShortForecastUrl = ultraShortForecastUrl;
    }

    @Override
    public String supportedDataCode() {
        return "ULTRA_SHORT_FORECAST";
    }

    @Override
    public CollectionResult collect(CollectionTarget target, CollectionJob job, int cyclesBack) {
        Long targetId = target.getTargetId();
        OffsetDateTime baseAt = calculateLatestBaseTime(OffsetDateTime.now(SEOUL_OFFSET))
                .minusHours(cyclesBack);
        String baseDate = baseAt.format(DATE_FORMATTER);
        String baseTime = baseAt.format(TIME_FORMATTER);

        List<LocationInfo> locations = locationInfoRepository.findAll();
        boolean anySuccess = false;
        long received = 0;
        long saved = 0;
        long duplicate = 0;

        for (LocationInfo location : locations) {
            try {
                List<Map<String, Object>> items = kmaVilageFcstClient.fetchVilageFcst(
                        ultraShortForecastUrl, location.getNx(), location.getNy(), baseDate, baseTime);
                long[] counts = saveForecastRows(targetId, job.getJobId(), location.getRegId(), baseAt, items);
                received += counts[0];
                saved += counts[1];
                duplicate += counts[2];
                anySuccess = true;
            } catch (Exception e) {
                log.warn("초단기예보 수집 실패: regId={}", location.getRegId(), e);
            }
        }

        return new CollectionResult(anySuccess, received, saved, duplicate);
    }

    private long[] saveForecastRows(Long targetId, Long jobId, String stnId, OffsetDateTime baseAt,
                                     List<Map<String, Object>> items) {
        Map<String, Map<String, String>> grouped = new LinkedHashMap<>();

        for (Map<String, Object> item : items) {
            String category = String.valueOf(item.get("category"));
            if (!SUPPORTED_CATEGORIES.contains(category)) {
                continue;
            }
            String fcstDate = String.valueOf(item.get("fcstDate"));
            String fcstTime = String.valueOf(item.get("fcstTime"));
            Map<String, String> group = grouped.computeIfAbsent(fcstDate + fcstTime, k -> new HashMap<>());
            group.put("fcstDate", fcstDate);
            group.put("fcstTime", fcstTime);
            group.put(category, String.valueOf(item.get("fcstValue")));
        }

        long[] counts = new long[3];
        for (Map<String, String> group : grouped.values()) {
            OffsetDateTime fcstAt = toOffsetDateTime(group.get("fcstDate"), group.get("fcstTime"));
            boolean inserted = ultraShortForecastRepository.upsert(jobId, targetId, stnId, baseAt, fcstAt,
                    toDecimal(group.get("POP")),
                    PrecipitationTextParser.parse(group.get("RN1")),
                    toDecimal(group.get("REH")),
                    toDecimal(group.get("T1H")),
                    toDecimal(group.get("VEC")),
                    toDecimal(group.get("WSD")),
                    toDecimal(group.get("LGT")));
            counts[0]++;
            counts[1]++;
            if (!inserted) {
                counts[2]++;
            }
        }
        return counts;
    }

    private BigDecimal toDecimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }

    private OffsetDateTime toOffsetDateTime(String date, String time) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
        LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);
        return OffsetDateTime.of(localDate, localTime, SEOUL_OFFSET);
    }

    private OffsetDateTime calculateLatestBaseTime(OffsetDateTime now) {
        OffsetDateTime candidate = now.withMinute(30).withSecond(0).withNano(0);
        if (now.isBefore(candidate.plusMinutes(15))) {
            candidate = candidate.minusHours(1);
        }
        return candidate;
    }
}
