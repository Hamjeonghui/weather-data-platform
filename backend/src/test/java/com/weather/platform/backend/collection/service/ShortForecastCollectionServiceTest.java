package com.weather.platform.backend.collection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.weather.platform.backend.collection.client.KmaShortForecastClient;
import com.weather.platform.backend.collection.entity.CollectionJob;
import com.weather.platform.backend.collection.entity.CollectionTarget;
import com.weather.platform.backend.forecast.repository.ShortForecastRepository;
import com.weather.platform.backend.location.entity.LocationInfo;
import com.weather.platform.backend.location.repository.LocationInfoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShortForecastCollectionServiceTest {

    @Mock
    private LocationInfoRepository locationInfoRepository;

    @Mock
    private ShortForecastRepository shortForecastRepository;

    @Mock
    private KmaShortForecastClient kmaShortForecastClient;

    @InjectMocks
    private ShortForecastCollectionService shortForecastCollectionService;

    @Test
    void 지원하는_데이터코드는_SHORT_FORECAST이다() {
        assertThat(shortForecastCollectionService.supportedDataCode()).isEqualTo("SHORT_FORECAST");
    }

    @Test
    void 강수량_텍스트를_규칙대로_숫자로_변환하고_정의되지_않은_카테고리는_무시한다() {
        CollectionTarget target = mock(CollectionTarget.class);
        given(target.getTargetId()).willReturn(1L);
        CollectionJob job = mock(CollectionJob.class);
        given(job.getJobId()).willReturn(10L);

        given(locationInfoRepository.findAll())
                .willReturn(List.of(new LocationInfo("11B10101", "1100000000", "서울특별시", 60L, 127L)));
        given(kmaShortForecastClient.fetchVilageFcst(anyLong(), anyLong(), anyString(), anyString()))
                .willReturn(List.of(
                        item("0600", "TMP", "25"),
                        item("0600", "PCP", "강수없음"),
                        item("0600", "SKY", "4"),
                        item("0700", "PCP", "1mm 미만"),
                        item("0800", "PCP", "3.0mm"),
                        item("0900", "PCP", "30.0~50.0mm"),
                        item("1000", "PCP", "50.0mm 이상")
                ));

        boolean result = shortForecastCollectionService.collect(target, job);

        assertThat(result).isTrue();

        ArgumentCaptor<BigDecimal> rnCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(shortForecastRepository, org.mockito.Mockito.times(5)).upsert(
                eq(10L), eq(1L), eq("11B10101"), any(), any(), any(), rnCaptor.capture(),
                any(), any(), any(), any(), any(), any());

        List<BigDecimal> rnValues = rnCaptor.getAllValues();
        assertThat(rnValues).containsExactly(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("3.0"),
                new BigDecimal("30.0"),
                new BigDecimal("50.0"));
    }

    @Test
    void 모든_지점_수집에_실패하면_false를_반환한다() {
        CollectionTarget target = mock(CollectionTarget.class);
        given(target.getTargetId()).willReturn(1L);
        CollectionJob job = mock(CollectionJob.class);

        given(locationInfoRepository.findAll())
                .willReturn(List.of(new LocationInfo("11B10101", "1100000000", "서울특별시", 60L, 127L)));
        given(kmaShortForecastClient.fetchVilageFcst(anyLong(), anyLong(), anyString(), anyString()))
                .willThrow(new RuntimeException("API 호출 실패"));

        boolean result = shortForecastCollectionService.collect(target, job);

        assertThat(result).isFalse();
    }

    private Map<String, Object> item(String fcstTime, String category, String fcstValue) {
        return Map.of(
                "fcstDate", "20260826",
                "fcstTime", fcstTime,
                "category", category,
                "fcstValue", fcstValue);
    }
}
