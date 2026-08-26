package com.weather.platform.backend.collection.client;

import com.weather.platform.backend.collection.client.dto.KmaResponse;
import com.weather.platform.backend.global.exception.BusinessException;
import com.weather.platform.backend.global.exception.ErrorCode;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KmaShortForecastClient {

    private static final Logger log = LoggerFactory.getLogger(KmaShortForecastClient.class);
    private static final String SUCCESS_RESULT_CODE = "00";
    private static final int NUM_OF_ROWS = 1000;

    private final RestClient restClient;
    private final String baseUrl;
    private final String serviceKey;

    public KmaShortForecastClient(RestClient.Builder restClientBuilder,
                                   @Value("${kma.short-forecast-url}") String baseUrl,
                                   @Value("${kma.service-key}") String serviceKey) {
        this.restClient = restClientBuilder.build();
        this.baseUrl = baseUrl;
        this.serviceKey = serviceKey;
    }

    public List<Map<String, Object>> fetchVilageFcst(long nx, long ny, String baseDate, String baseTime) {
        List<Map<String, Object>> allItems = new ArrayList<>();
        int pageNo = 1;

        while (true) {
            KmaResponse response = call(nx, ny, baseDate, baseTime, pageNo);
            validate(response, nx, ny, baseDate, baseTime);

            List<Map<String, Object>> items = response.response().body().items() == null
                    ? List.of()
                    : response.response().body().items().item();
            if (items == null || items.isEmpty()) {
                break;
            }
            allItems.addAll(items);

            Integer totalCount = response.response().body().totalCount();
            if (totalCount == null || allItems.size() >= totalCount) {
                break;
            }
            pageNo++;
        }

        if (allItems.isEmpty()) {
            log.warn("단기예보 API 응답에 자료 없음: nx={}, ny={}, baseDate={}, baseTime={}", nx, ny, baseDate, baseTime);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }

        return allItems;
    }

    private KmaResponse call(long nx, long ny, String baseDate, String baseTime, int pageNo) {
        URI uri = URI.create(baseUrl
                + "?serviceKey=" + serviceKey
                + "&pageNo=" + pageNo
                + "&numOfRows=" + NUM_OF_ROWS
                + "&dataType=JSON"
                + "&base_date=" + baseDate
                + "&base_time=" + baseTime
                + "&nx=" + nx
                + "&ny=" + ny);

        try {
            return restClient.get().uri(uri).retrieve().body(KmaResponse.class);
        } catch (RestClientException e) {
            log.warn("단기예보 API 호출 실패: nx={}, ny={}, baseDate={}, baseTime={}", nx, ny, baseDate, baseTime, e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private void validate(KmaResponse response, long nx, long ny, String baseDate, String baseTime) {
        if (response == null || response.response() == null || response.response().header() == null
                || !SUCCESS_RESULT_CODE.equals(response.response().header().resultCode())) {
            log.warn("단기예보 API 응답 실패: nx={}, ny={}, baseDate={}, baseTime={}, response={}",
                    nx, ny, baseDate, baseTime, response);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
}
