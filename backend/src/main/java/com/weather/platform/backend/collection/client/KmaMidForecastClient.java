package com.weather.platform.backend.collection.client;

import com.weather.platform.backend.collection.client.dto.KmaResponse;
import com.weather.platform.backend.global.exception.BusinessException;
import com.weather.platform.backend.global.exception.ErrorCode;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KmaMidForecastClient {

    private static final Logger log = LoggerFactory.getLogger(KmaMidForecastClient.class);
    private static final String SUCCESS_RESULT_CODE = "00";

    private final RestClient restClient;
    private final String baseUrl;
    private final String serviceKey;

    public KmaMidForecastClient(RestClient.Builder restClientBuilder,
                                 @Value("${kma.mid-forecast-url}") String baseUrl,
                                 @Value("${kma.service-key}") String serviceKey) {
        this.restClient = restClientBuilder.build();
        this.baseUrl = baseUrl;
        this.serviceKey = serviceKey;
    }

    public Map<String, Object> fetchMidLandFcst(String regId, String tmFc) {
        URI uri = URI.create(baseUrl
                + "?serviceKey=" + serviceKey
                + "&pageNo=1"
                + "&numOfRows=10"
                + "&dataType=JSON"
                + "&regId=" + regId
                + "&tmFc=" + tmFc);

        KmaResponse response;
        try {
            response = restClient.get().uri(uri).retrieve().body(KmaResponse.class);
        } catch (RestClientException e) {
            log.warn("중기예보 API 호출 실패: regId={}, tmFc={}", regId, tmFc, e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }

        if (response == null || response.response() == null || response.response().header() == null
                || !SUCCESS_RESULT_CODE.equals(response.response().header().resultCode())) {
            log.warn("중기예보 API 응답 실패: regId={}, tmFc={}, response={}", regId, tmFc, response);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }

        List<Map<String, Object>> items = response.response().body() == null || response.response().body().items() == null
                ? List.of()
                : response.response().body().items().item();

        if (items == null || items.isEmpty()) {
            log.warn("중기예보 API 응답에 자료 없음: regId={}, tmFc={}", regId, tmFc);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }

        return items.get(0);
    }
}
