package com.weather.platform.backend.collection.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.weather.platform.backend.global.exception.BusinessException;
import com.weather.platform.backend.global.exception.ErrorCode;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class KmaShortForecastClientTest {

    private static final String BASE_URL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";

    private KmaShortForecastClient buildClient(MockRestServiceServer[] serverHolder) {
        RestClient.Builder builder = RestClient.builder();
        serverHolder[0] = MockRestServiceServer.bindTo(builder).build();
        return new KmaShortForecastClient(builder, BASE_URL, "test-key");
    }

    @Test
    void 총_건수가_numOfRows보다_많으면_다음_페이지를_이어서_조회한다() {
        MockRestServiceServer[] serverHolder = new MockRestServiceServer[1];
        KmaShortForecastClient client = buildClient(serverHolder);
        MockRestServiceServer server = serverHolder[0];

        server.expect(requestTo(org.hamcrest.Matchers.containsString("pageNo=1")))
                .andRespond(withSuccess(pageResponse(1, 1000, 1500), MediaType.APPLICATION_JSON));
        server.expect(requestTo(org.hamcrest.Matchers.containsString("pageNo=2")))
                .andRespond(withSuccess(pageResponse(2, 1000, 1500), MediaType.APPLICATION_JSON));

        var items = client.fetchVilageFcst(60, 127, "20260826", "0500");

        assertThat(items).hasSize(1500);
        server.verify();
    }

    @Test
    void 결과코드가_실패이면_EXTERNAL_API_ERROR_예외가_발생한다() {
        MockRestServiceServer[] serverHolder = new MockRestServiceServer[1];
        KmaShortForecastClient client = buildClient(serverHolder);
        MockRestServiceServer server = serverHolder[0];

        server.expect(requestTo(org.hamcrest.Matchers.containsString("pageNo=1")))
                .andRespond(withSuccess("""
                        {"response":{"header":{"resultCode":"03","resultMsg":"NODATA_ERROR"}}}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchVilageFcst(60, 127, "20260826", "0500"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.EXTERNAL_API_ERROR);
    }

    private String pageResponse(int pageNo, int numOfRows, int totalCount) {
        int remaining = totalCount - (pageNo - 1) * numOfRows;
        int itemCountInPage = Math.min(numOfRows, remaining);

        StringBuilder items = new StringBuilder();
        for (int i = 0; i < itemCountInPage; i++) {
            if (i > 0) {
                items.append(",");
            }
            items.append(Map.of("category", "TMP", "fcstDate", "20260826", "fcstTime", "0600", "fcstValue", "25")
                    .entrySet().stream()
                    .map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"")
                    .reduce((a, b) -> a + "," + b)
                    .map(s -> "{" + s + "}")
                    .orElse("{}"));
        }

        return """
                {"response":{"header":{"resultCode":"00","resultMsg":"NORMAL_SERVICE"},
                "body":{"dataType":"JSON","items":{"item":[%s]},"pageNo":%d,"numOfRows":%d,"totalCount":%d}}}
                """.formatted(items, pageNo, numOfRows, totalCount);
    }
}
