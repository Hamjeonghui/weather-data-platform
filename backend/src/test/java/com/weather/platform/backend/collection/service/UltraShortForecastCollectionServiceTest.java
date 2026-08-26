package com.weather.platform.backend.collection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.weather.platform.backend.collection.client.KmaVilageFcstClient;
import com.weather.platform.backend.collection.entity.CollectionJob;
import com.weather.platform.backend.collection.entity.CollectionTarget;
import com.weather.platform.backend.forecast.repository.UltraShortForecastRepository;
import com.weather.platform.backend.location.entity.LocationInfo;
import com.weather.platform.backend.location.repository.LocationInfoRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UltraShortForecastCollectionServiceTest {

    @Mock
    private LocationInfoRepository locationInfoRepository;

    @Mock
    private UltraShortForecastRepository ultraShortForecastRepository;

    @Mock
    private KmaVilageFcstClient kmaVilageFcstClient;

    @InjectMocks
    private UltraShortForecastCollectionService ultraShortForecastCollectionService;

    @Test
    void 지원하는_데이터코드는_ULTRA_SHORT_FORECAST이다() {
        assertThat(ultraShortForecastCollectionService.supportedDataCode()).isEqualTo("ULTRA_SHORT_FORECAST");
    }

    @Test
    void 강수량_텍스트를_규칙대로_숫자로_변환하고_정의되지_않은_카테고리는_무시한다() {
        CollectionTarget target = mock(CollectionTarget.class);
        given(target.getTargetId()).willReturn(1L);
        CollectionJob job = mock(CollectionJob.class);
        given(job.getJobId()).willReturn(10L);

        given(locationInfoRepository.findAll())
                .willReturn(List.of(new LocationInfo("11B10101", "1100000000", "서울특별시", 60L, 127L)));
        given(kmaVilageFcstClient.fetchVilageFcst(any(), anyLong(), anyLong(), anyString(), anyString()))
                .willReturn(List.of(
                        item("1500", "T1H", "29"),
                        item("1500", "LGT", "0"),
                        item("1500", "RN1", "강수없음"),
                        item("1500", "SKY", "4"),
                        item("1600", "RN1", "1.0mm")
                ));
        given(ultraShortForecastRepository.upsert(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(true);

        CollectionResult result = ultraShortForecastCollectionService.collect(target, job, 0);

        assertThat(result.anySuccess()).isTrue();
        assertThat(result.savedCount()).isEqualTo(2);
        assertThat(result.duplicateCount()).isEqualTo(0);

        ArgumentCaptor<BigDecimal> rnCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(ultraShortForecastRepository, times(2)).upsert(
                eq(10L), eq(1L), eq("11B10101"), any(), any(), any(), rnCaptor.capture(), any(), any(), any(), any(), any());

        assertThat(rnCaptor.getAllValues()).containsExactly(BigDecimal.ZERO, new BigDecimal("1.0"));
    }

    @Test
    void 이미_존재하는_행을_갱신하면_duplicateCount에_반영된다() {
        CollectionTarget target = mock(CollectionTarget.class);
        given(target.getTargetId()).willReturn(1L);
        CollectionJob job = mock(CollectionJob.class);
        given(job.getJobId()).willReturn(10L);

        given(locationInfoRepository.findAll())
                .willReturn(List.of(new LocationInfo("11B10101", "1100000000", "서울특별시", 60L, 127L)));
        given(kmaVilageFcstClient.fetchVilageFcst(any(), anyLong(), anyLong(), anyString(), anyString()))
                .willReturn(List.of(item("1500", "T1H", "29"), item("1600", "T1H", "28")));
        given(ultraShortForecastRepository.upsert(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(true, false);

        CollectionResult result = ultraShortForecastCollectionService.collect(target, job, 0);

        assertThat(result.receivedCount()).isEqualTo(2);
        assertThat(result.savedCount()).isEqualTo(2);
        assertThat(result.duplicateCount()).isEqualTo(1);
    }

    @Test
    void 모든_지점_수집에_실패하면_결과가_실패이다() {
        CollectionTarget target = mock(CollectionTarget.class);
        given(target.getTargetId()).willReturn(1L);
        CollectionJob job = mock(CollectionJob.class);

        given(locationInfoRepository.findAll())
                .willReturn(List.of(new LocationInfo("11B10101", "1100000000", "서울특별시", 60L, 127L)));
        given(kmaVilageFcstClient.fetchVilageFcst(any(), anyLong(), anyLong(), anyString(), anyString()))
                .willThrow(new RuntimeException("API 호출 실패"));

        CollectionResult result = ultraShortForecastCollectionService.collect(target, job, 0);

        assertThat(result.anySuccess()).isFalse();
    }

    @Test
    void cyclesBack이_1이면_한_주기_이전_발표시각으로_조회한다() {
        CollectionTarget target = mock(CollectionTarget.class);
        given(target.getTargetId()).willReturn(1L);
        CollectionJob job = mock(CollectionJob.class);

        given(locationInfoRepository.findAll())
                .willReturn(List.of(new LocationInfo("11B10101", "1100000000", "서울특별시", 60L, 127L)));
        given(kmaVilageFcstClient.fetchVilageFcst(any(), anyLong(), anyLong(), anyString(), anyString()))
                .willReturn(List.of());

        ultraShortForecastCollectionService.collect(target, job, 0);
        ultraShortForecastCollectionService.collect(target, job, 1);

        ArgumentCaptor<String> dateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> timeCaptor = ArgumentCaptor.forClass(String.class);
        verify(kmaVilageFcstClient, times(2)).fetchVilageFcst(
                any(), anyLong(), anyLong(), dateCaptor.capture(), timeCaptor.capture());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        LocalDateTime latest = LocalDateTime.parse(dateCaptor.getAllValues().get(0) + timeCaptor.getAllValues().get(0), formatter);
        LocalDateTime oneCycleBack = LocalDateTime.parse(dateCaptor.getAllValues().get(1) + timeCaptor.getAllValues().get(1), formatter);
        assertThat(Duration.between(oneCycleBack, latest)).isEqualTo(Duration.ofHours(1));
    }

    private Map<String, Object> item(String fcstTime, String category, String fcstValue) {
        return Map.of(
                "fcstDate", "20260826",
                "fcstTime", fcstTime,
                "category", category,
                "fcstValue", fcstValue);
    }
}
