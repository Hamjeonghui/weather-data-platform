package com.weather.platform.backend.collection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.weather.platform.backend.collection.client.KmaMidForecastClient;
import com.weather.platform.backend.collection.entity.CollectionJob;
import com.weather.platform.backend.collection.entity.CollectionTarget;
import com.weather.platform.backend.forecast.repository.MidForecastRepository;
import com.weather.platform.backend.global.exception.BusinessException;
import com.weather.platform.backend.global.exception.ErrorCode;
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
class MidForecastCollectionServiceTest {

    @Mock
    private LocationInfoRepository locationInfoRepository;

    @Mock
    private MidForecastRepository midForecastRepository;

    @Mock
    private KmaMidForecastClient kmaMidForecastClient;

    @InjectMocks
    private MidForecastCollectionService midForecastCollectionService;

    @Test
    void 지원하는_데이터코드는_MID_FORECAST이다() {
        assertThat(midForecastCollectionService.supportedDataCode()).isEqualTo("MID_FORECAST");
    }

    @Test
    void 지점_수집에_성공하면_예보값을_병합해_저장하고_결과를_반환한다() {
        CollectionTarget target = mock(CollectionTarget.class);
        given(target.getTargetId()).willReturn(1L);
        CollectionJob job = mock(CollectionJob.class);
        given(job.getJobId()).willReturn(10L);

        given(locationInfoRepository.findAll())
                .willReturn(List.of(new LocationInfo("11B10101", "1100000000", "서울특별시", 60L, 127L)));
        given(kmaMidForecastClient.fetchMidLandFcst(eq("11B10101"), anyString()))
                .willReturn(Map.of("rnSt3Am", 40, "wf3Am", "구름많음"));
        given(midForecastRepository.upsert(any(), any(), any(), any(), any(), any(), any(), any())).willReturn(true);

        CollectionResult result = midForecastCollectionService.collect(target, job, 0);

        assertThat(result.anySuccess()).isTrue();
        assertThat(result.receivedCount()).isEqualTo(1);
        assertThat(result.savedCount()).isEqualTo(1);
        assertThat(result.duplicateCount()).isEqualTo(0);
        verify(midForecastRepository).upsert(eq(10L), eq(1L), eq("11B10101"), any(), any(),
                eq(BigDecimal.valueOf(40)), eq("구름많음"), eq("AM"));
        verify(midForecastRepository, never()).upsert(any(), any(), any(), any(), any(), any(), any(), eq("PM"));
    }

    @Test
    void 이미_존재하는_행을_갱신하면_duplicateCount에_반영된다() {
        CollectionTarget target = mock(CollectionTarget.class);
        given(target.getTargetId()).willReturn(1L);
        CollectionJob job = mock(CollectionJob.class);
        given(job.getJobId()).willReturn(10L);

        given(locationInfoRepository.findAll())
                .willReturn(List.of(new LocationInfo("11B10101", "1100000000", "서울특별시", 60L, 127L)));
        given(kmaMidForecastClient.fetchMidLandFcst(eq("11B10101"), anyString()))
                .willReturn(Map.of("rnSt3Am", 40, "wf3Am", "구름많음", "rnSt3Pm", 50, "wf3Pm", "맑음"));
        given(midForecastRepository.upsert(any(), any(), any(), any(), any(), any(), any(), eq("AM")))
                .willReturn(true);
        given(midForecastRepository.upsert(any(), any(), any(), any(), any(), any(), any(), eq("PM")))
                .willReturn(false);

        CollectionResult result = midForecastCollectionService.collect(target, job, 0);

        assertThat(result.receivedCount()).isEqualTo(2);
        assertThat(result.savedCount()).isEqualTo(2);
        assertThat(result.duplicateCount()).isEqualTo(1);
    }

    @Test
    void 모든_지점_수집에_실패하면_결과가_실패이고_저장하지_않는다() {
        CollectionTarget target = mock(CollectionTarget.class);
        given(target.getTargetId()).willReturn(1L);
        CollectionJob job = mock(CollectionJob.class);

        given(locationInfoRepository.findAll())
                .willReturn(List.of(new LocationInfo("11B10101", "1100000000", "서울특별시", 60L, 127L)));
        given(kmaMidForecastClient.fetchMidLandFcst(eq("11B10101"), anyString()))
                .willThrow(new BusinessException(ErrorCode.EXTERNAL_API_ERROR));

        CollectionResult result = midForecastCollectionService.collect(target, job, 0);

        assertThat(result.anySuccess()).isFalse();
        assertThat(result.savedCount()).isEqualTo(0);
        verify(midForecastRepository, never()).upsert(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void cyclesBack이_1이면_한_주기_이전_발표시각으로_조회한다() {
        CollectionTarget target = mock(CollectionTarget.class);
        given(target.getTargetId()).willReturn(1L);
        CollectionJob job = mock(CollectionJob.class);

        given(locationInfoRepository.findAll())
                .willReturn(List.of(new LocationInfo("11B10101", "1100000000", "서울특별시", 60L, 127L)));
        given(kmaMidForecastClient.fetchMidLandFcst(eq("11B10101"), anyString())).willReturn(Map.of());

        midForecastCollectionService.collect(target, job, 0);
        midForecastCollectionService.collect(target, job, 1);

        ArgumentCaptor<String> tmFcCaptor = ArgumentCaptor.forClass(String.class);
        verify(kmaMidForecastClient, times(2)).fetchMidLandFcst(eq("11B10101"), tmFcCaptor.capture());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        LocalDateTime latest = LocalDateTime.parse(tmFcCaptor.getAllValues().get(0), formatter);
        LocalDateTime oneCycleBack = LocalDateTime.parse(tmFcCaptor.getAllValues().get(1), formatter);
        assertThat(Duration.between(oneCycleBack, latest)).isEqualTo(Duration.ofHours(12));
    }
}
