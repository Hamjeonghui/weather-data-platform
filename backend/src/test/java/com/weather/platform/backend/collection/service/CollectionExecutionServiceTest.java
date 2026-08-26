package com.weather.platform.backend.collection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.weather.platform.backend.collection.dto.ExecuteCollectionResponse;
import com.weather.platform.backend.collection.entity.CollectionJob;
import com.weather.platform.backend.collection.entity.CollectionStatus;
import com.weather.platform.backend.collection.entity.CollectionTarget;
import com.weather.platform.backend.collection.repository.CollectionJobRepository;
import com.weather.platform.backend.collection.repository.CollectionTargetRepository;
import com.weather.platform.backend.global.exception.BusinessException;
import com.weather.platform.backend.global.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CollectionExecutionServiceTest {

    @Mock
    private CollectionTargetRepository collectionTargetRepository;

    @Mock
    private CollectionJobRepository collectionJobRepository;

    @Mock
    private CollectionExecutor collectionExecutor;

    private CollectionTarget targetWithDataCode(String dataCode) {
        CollectionTarget target = mock(CollectionTarget.class);
        given(target.getDataCode()).willReturn(dataCode);
        return target;
    }

    @Test
    void 존재하지_않는_수집대상이면_COLLECTION_TARGET_NOT_FOUND_예외가_발생한다() {
        CollectionExecutionService collectionExecutionService = new CollectionExecutionService(
                collectionTargetRepository, collectionJobRepository, List.of());
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> collectionExecutionService.execute(1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.COLLECTION_TARGET_NOT_FOUND);
    }

    @Test
    void 이미_실행중인_작업이_있으면_COLLECTION_ALREADY_RUNNING_예외가_발생한다() {
        CollectionExecutionService collectionExecutionService = new CollectionExecutionService(
                collectionTargetRepository, collectionJobRepository, List.of());
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.of(mock(CollectionTarget.class)));
        given(collectionJobRepository.existsByTargetIdAndStatus(1L, CollectionStatus.RUNNING)).willReturn(true);

        assertThatThrownBy(() -> collectionExecutionService.execute(1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.COLLECTION_ALREADY_RUNNING);

        verify(collectionJobRepository, never()).save(any());
    }

    @Test
    void 데이터코드에_맞는_실행기가_없으면_INTERNAL_SERVER_ERROR_예외가_발생한다() {
        given(collectionExecutor.supportedDataCode()).willReturn("MID_FORECAST");
        CollectionExecutionService collectionExecutionService = new CollectionExecutionService(
                collectionTargetRepository, collectionJobRepository, List.of(collectionExecutor));
        CollectionTarget target = targetWithDataCode("SHORT_FORECAST");
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.of(target));
        given(collectionJobRepository.existsByTargetIdAndStatus(1L, CollectionStatus.RUNNING)).willReturn(false);

        assertThatThrownBy(() -> collectionExecutionService.execute(1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Test
    void 실행기가_바로_성공하면_재시도_없이_SUCCESS이고_수집_건수가_job에_기록된다() {
        given(collectionExecutor.supportedDataCode()).willReturn("MID_FORECAST");
        CollectionExecutionService collectionExecutionService = new CollectionExecutionService(
                collectionTargetRepository, collectionJobRepository, List.of(collectionExecutor));
        CollectionTarget target = targetWithDataCode("MID_FORECAST");
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.of(target));
        given(collectionJobRepository.existsByTargetIdAndStatus(1L, CollectionStatus.RUNNING)).willReturn(false);
        ArgumentCaptor<CollectionJob> jobCaptor = ArgumentCaptor.forClass(CollectionJob.class);
        given(collectionJobRepository.save(jobCaptor.capture())).willAnswer(invocation -> invocation.getArgument(0));
        given(collectionExecutor.collect(any(), any(), anyInt())).willReturn(new CollectionResult(true, 176, 176, 3));

        ExecuteCollectionResponse response = collectionExecutionService.execute(1L, "admin");

        assertThat(response.status()).isEqualTo(CollectionStatus.SUCCESS.name());
        verify(collectionExecutor, times(1)).collect(any(), any(), anyInt());
        verify(collectionJobRepository, times(1)).save(any());

        CollectionJob savedJob = jobCaptor.getValue();
        assertThat(savedJob.getReceivedCount()).isEqualTo(176L);
        assertThat(savedJob.getSavedCount()).isEqualTo(176L);
        assertThat(savedJob.getDuplicateCount()).isEqualTo(3L);
    }

    @Test
    void 첫_시도가_실패하면_직전_주기로_한_번_재시도하고_성공하면_재시도_작업이_retryOfJobId를_갖는다() {
        given(collectionExecutor.supportedDataCode()).willReturn("MID_FORECAST");
        CollectionExecutionService collectionExecutionService = new CollectionExecutionService(
                collectionTargetRepository, collectionJobRepository, List.of(collectionExecutor));
        CollectionTarget target = targetWithDataCode("MID_FORECAST");
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.of(target));
        given(collectionJobRepository.existsByTargetIdAndStatus(1L, CollectionStatus.RUNNING)).willReturn(false);

        CollectionJob firstJobMock = mock(CollectionJob.class);
        given(firstJobMock.getJobId()).willReturn(100L);
        given(collectionJobRepository.save(any()))
                .willReturn(firstJobMock)
                .willAnswer(invocation -> invocation.getArgument(0));

        given(collectionExecutor.collect(any(), any(), eq(0))).willReturn(CollectionResult.empty());
        given(collectionExecutor.collect(any(), any(), eq(1))).willReturn(new CollectionResult(true, 176, 176, 0));

        ExecuteCollectionResponse response = collectionExecutionService.execute(1L, "admin");

        assertThat(response.status()).isEqualTo(CollectionStatus.SUCCESS.name());
        verify(collectionExecutor, times(2)).collect(any(), any(), anyInt());

        ArgumentCaptor<CollectionJob> jobCaptor = ArgumentCaptor.forClass(CollectionJob.class);
        verify(collectionJobRepository, times(2)).save(jobCaptor.capture());
        CollectionJob retryJob = jobCaptor.getAllValues().get(1);
        assertThat(retryJob.getRetryOfJobId()).isEqualTo(100L);
        assertThat(retryJob.getSavedCount()).isEqualTo(176L);
    }

    @Test
    void 재시도까지_모두_실패하면_작업_상태가_FAILED이고_재시도는_한_번만_일어난다() {
        given(collectionExecutor.supportedDataCode()).willReturn("MID_FORECAST");
        CollectionExecutionService collectionExecutionService = new CollectionExecutionService(
                collectionTargetRepository, collectionJobRepository, List.of(collectionExecutor));
        CollectionTarget target = targetWithDataCode("MID_FORECAST");
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.of(target));
        given(collectionJobRepository.existsByTargetIdAndStatus(1L, CollectionStatus.RUNNING)).willReturn(false);
        given(collectionJobRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(collectionExecutor.collect(any(), any(), anyInt())).willReturn(CollectionResult.empty());

        ExecuteCollectionResponse response = collectionExecutionService.execute(1L, "admin");

        assertThat(response.status()).isEqualTo(CollectionStatus.FAILED.name());
        verify(collectionExecutor, times(2)).collect(any(), any(), anyInt());
        verify(collectionJobRepository, times(2)).save(any());
    }
}
