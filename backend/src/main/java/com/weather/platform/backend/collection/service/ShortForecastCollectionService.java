package com.weather.platform.backend.collection.service;

import com.weather.platform.backend.collection.client.KmaShortForecastClient;
import com.weather.platform.backend.collection.entity.CollectionJob;
import com.weather.platform.backend.collection.entity.CollectionTarget;
import com.weather.platform.backend.forecast.repository.ShortForecastRepository;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ShortForecastCollectionService implements CollectionExecutor {

    private static final Logger log = LoggerFactory.getLogger(ShortForecastCollectionService.class);

    private static final ZoneOffset SEOUL_OFFSET = ZoneOffset.of("+09:00");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");
    private static final int[] BASE_HOURS = {2, 5, 8, 11, 14, 17, 20, 23};
    private static final Set<String> SUPPORTED_CATEGORIES =
            Set.of("POP", "PCP", "REH", "TMP", "TMN", "TMX", "VEC", "WSD");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9]+(\\.[0-9]+)?)");

    private final LocationInfoRepository locationInfoRepository;
    private final ShortForecastRepository shortForecastRepository;
    private final KmaShortForecastClient kmaShortForecastClient;

    public ShortForecastCollectionService(LocationInfoRepository locationInfoRepository,
                                           ShortForecastRepository shortForecastRepository,
                                           KmaShortForecastClient kmaShortForecastClient) {
        this.locationInfoRepository = locationInfoRepository;
        this.shortForecastRepository = shortForecastRepository;
        this.kmaShortForecastClient = kmaShortForecastClient;
    }

    @Override
    public String supportedDataCode() {
        return "SHORT_FORECAST";
    }

    @Override
    public boolean collect(CollectionTarget target, CollectionJob job) {
        Long targetId = target.getTargetId();
        OffsetDateTime baseAt = calculateLatestBaseTime(OffsetDateTime.now(SEOUL_OFFSET));
        String baseDate = baseAt.format(DATE_FORMATTER);
        String baseTime = baseAt.format(TIME_FORMATTER);

        List<LocationInfo> locations = locationInfoRepository.findAll();
        boolean anySuccess = false;

        for (LocationInfo location : locations) {
            try {
                List<Map<String, Object>> items = kmaShortForecastClient.fetchVilageFcst(
                        location.getNx(), location.getNy(), baseDate, baseTime);
                saveForecastRows(targetId, job.getJobId(), location.getRegId(), baseAt, items);
                anySuccess = true;
            } catch (Exception e) {
                log.warn("단기예보 수집 실패: regId={}", location.getRegId(), e);
            }
        }

        return anySuccess;
    }

    private void saveForecastRows(Long targetId, Long jobId, String stnId, OffsetDateTime baseAt,
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

        for (Map<String, String> group : grouped.values()) {
            OffsetDateTime fcstAt = toOffsetDateTime(group.get("fcstDate"), group.get("fcstTime"));
            shortForecastRepository.upsert(jobId, targetId, stnId, baseAt, fcstAt,
                    toDecimal(group.get("POP")),
                    parsePcp(group.get("PCP")),
                    toDecimal(group.get("REH")),
                    toDecimal(group.get("TMP")),
                    toDecimal(group.get("TMN")),
                    toDecimal(group.get("TMX")),
                    toDecimal(group.get("VEC")),
                    toDecimal(group.get("WSD")));
        }
    }

    private BigDecimal toDecimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }

    private BigDecimal parsePcp(String value) {
        if (value == null) {
            return null;
        }
        if (value.contains("강수없음") || value.contains("미만")) {
            return BigDecimal.ZERO;
        }
        Matcher matcher = NUMBER_PATTERN.matcher(value);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        return null;
    }

    private OffsetDateTime toOffsetDateTime(String date, String time) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
        LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);
        return OffsetDateTime.of(localDate, localTime, SEOUL_OFFSET);
    }

    private OffsetDateTime calculateLatestBaseTime(OffsetDateTime now) {
        for (int i = BASE_HOURS.length - 1; i >= 0; i--) {
            OffsetDateTime candidate = now.withHour(BASE_HOURS[i]).withMinute(0).withSecond(0).withNano(0);
            if (!now.isBefore(candidate.plusMinutes(10))) {
                return candidate;
            }
        }
        return now.minusDays(1).withHour(23).withMinute(0).withSecond(0).withNano(0);
    }
}
