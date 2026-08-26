package com.weather.platform.backend.collection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.weather.platform.backend.collection.client.KmaMidForecastClient;
import com.weather.platform.backend.collection.dto.ExecuteCollectionResponse;
import com.weather.platform.backend.collection.entity.CollectionStatus;
import com.weather.platform.backend.collection.entity.CollectionTarget;
import com.weather.platform.backend.collection.repository.CollectionJobRepository;
import com.weather.platform.backend.collection.repository.CollectionTargetRepository;
import com.weather.platform.backend.forecast.repository.MidForecastRepository;
import com.weather.platform.backend.global.exception.BusinessException;
import com.weather.platform.backend.global.exception.ErrorCode;
import com.weather.platform.backend.location.entity.LocationInfo;
import com.weather.platform.backend.location.repository.LocationInfoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MidForecastCollectionServiceTest {

    @Mock
    private CollectionTargetRepository collectionTargetRepository;

    @Mock
    private CollectionJobRepository collectionJobRepository;

    @Mock
    private LocationInfoRepository locationInfoRepository;

    @Mock
    private MidForecastRepository midForecastRepository;

    @Mock
    private KmaMidForecastClient kmaMidForecastClient;

    @InjectMocks
    private MidForecastCollectionService midForecastCollectionService;

    @Test
    void 존재하지_않는_수집대상이면_COLLECTION_TARGET_NOT_FOUND_예외가_발생한다() {
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> midForecastCollectionService.execute(1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.COLLECTION_TARGET_NOT_FOUND);
    }

    @Test
    void 이미_실행중인_작업이_있으면_COLLECTION_ALREADY_RUNNING_예외가_발생한다() {
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.of(mock(CollectionTarget.class)));
        given(collectionJobRepository.existsByTargetIdAndStatus(1L, CollectionStatus.RUNNING)).willReturn(true);

        assertThatThrownBy(() -> midForecastCollectionService.execute(1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.COLLECTION_ALREADY_RUNNING);

        verify(locationInfoRepository, never()).findAll();
    }

    @Test
    void 지점_수집에_성공하면_예보값을_병합해_저장하고_작업_상태가_SUCCESS이다() {
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.of(mock(CollectionTarget.class)));
        given(collectionJobRepository.existsByTargetIdAndStatus(1L, CollectionStatus.RUNNING)).willReturn(false);
        given(collectionJobRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(locationInfoRepository.findAll())
                .willReturn(List.of(new LocationInfo("11B10101", "1100000000", "서울특별시", 60L, 127L)));
        given(kmaMidForecastClient.fetchMidLandFcst(eq("11B10101"), anyString()))
                .willReturn(Map.of("rnSt3Am", 40, "wf3Am", "구름많음"));

        ExecuteCollectionResponse response = midForecastCollectionService.execute(1L, "admin");

        assertThat(response.status()).isEqualTo(CollectionStatus.SUCCESS.name());
        verify(midForecastRepository).upsert(any(), eq(1L), eq("11B10101"), any(), any(),
                eq(BigDecimal.valueOf(40)), eq("구름많음"), eq("AM"));
        verify(midForecastRepository, never()).upsert(any(), any(), any(), any(), any(), any(), any(), eq("PM"));
    }

    @Test
    void 모든_지점_수집에_실패하면_작업_상태가_FAILED이고_저장하지_않는다() {
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.of(mock(CollectionTarget.class)));
        given(collectionJobRepository.existsByTargetIdAndStatus(1L, CollectionStatus.RUNNING)).willReturn(false);
        given(collectionJobRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(locationInfoRepository.findAll())
                .willReturn(List.of(new LocationInfo("11B10101", "1100000000", "서울특별시", 60L, 127L)));
        given(kmaMidForecastClient.fetchMidLandFcst(eq("11B10101"), anyString()))
                .willThrow(new BusinessException(ErrorCode.EXTERNAL_API_ERROR));

        ExecuteCollectionResponse response = midForecastCollectionService.execute(1L, "admin");

        assertThat(response.status()).isEqualTo(CollectionStatus.FAILED.name());
        verify(midForecastRepository, never()).upsert(any(), any(), any(), any(), any(), any(), any(), any());
    }
}
