package com.weather.platform.backend.collection.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.weather.platform.backend.collection.entity.CollectionJob;
import com.weather.platform.backend.collection.entity.CollectionTarget;
import com.weather.platform.backend.collection.entity.TriggerType;
import com.weather.platform.backend.collection.repository.CollectionJobRepository;
import com.weather.platform.backend.collection.repository.CollectionTargetRepository;
import com.weather.platform.backend.collection.service.CollectionExecutionService;
import com.weather.platform.backend.global.exception.BusinessException;
import com.weather.platform.backend.global.exception.ErrorCode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CollectionSchedulerTest {

    private static final ZoneOffset SEOUL_OFFSET = ZoneOffset.of("+09:00");

    @Mock
    private CollectionTargetRepository collectionTargetRepository;

    @Mock
    private CollectionJobRepository collectionJobRepository;

    @Mock
    private CollectionExecutionService collectionExecutionService;

    @InjectMocks
    private CollectionScheduler collectionScheduler;

    private CollectionTarget target(Long targetId, String scheduleType, int intervalValue) {
        CollectionTarget target = mock(CollectionTarget.class);
        given(target.getTargetId()).willReturn(targetId);
        org.mockito.Mockito.lenient().when(target.getScheduleType()).thenReturn(scheduleType);
        org.mockito.Mockito.lenient().when(target.getIntervalValue()).thenReturn(intervalValue);
        return target;
    }

    @Test
    void 이전_실행_이력이_없으면_즉시_실행한다() {
        CollectionTarget target = target(1L, "HOUR", 12);
        given(collectionTargetRepository.findByEnabledTrue()).willReturn(List.of(target));
        given(collectionJobRepository.findFirstByTargetIdOrderByStartedAtDesc(1L)).willReturn(Optional.empty());

        collectionScheduler.run();

        verify(collectionExecutionService).execute(1L, null, TriggerType.SCHEDULED);
    }

    @Test
    void 간격이_지나지_않았으면_실행하지_않는다() {
        CollectionTarget target = target(1L, "HOUR", 12);
        given(collectionTargetRepository.findByEnabledTrue()).willReturn(List.of(target));
        CollectionJob lastJob = mock(CollectionJob.class);
        given(lastJob.getStartedAt()).willReturn(OffsetDateTime.now(SEOUL_OFFSET).minusHours(1));
        given(collectionJobRepository.findFirstByTargetIdOrderByStartedAtDesc(1L)).willReturn(Optional.of(lastJob));

        collectionScheduler.run();

        verify(collectionExecutionService, never()).execute(anyLong(), isNull(), any());
    }

    @Test
    void 간격이_지났으면_실행한다() {
        CollectionTarget target = target(1L, "HOUR", 12);
        given(collectionTargetRepository.findByEnabledTrue()).willReturn(List.of(target));
        CollectionJob lastJob = mock(CollectionJob.class);
        given(lastJob.getStartedAt()).willReturn(OffsetDateTime.now(SEOUL_OFFSET).minusHours(13));
        given(collectionJobRepository.findFirstByTargetIdOrderByStartedAtDesc(1L)).willReturn(Optional.of(lastJob));

        collectionScheduler.run();

        verify(collectionExecutionService).execute(1L, null, TriggerType.SCHEDULED);
    }

    @Test
    void 비활성화된_대상은_조회되지_않으므로_실행되지_않는다() {
        given(collectionTargetRepository.findByEnabledTrue()).willReturn(List.of());

        collectionScheduler.run();

        verify(collectionExecutionService, never()).execute(anyLong(), isNull(), any());
        verify(collectionJobRepository, never()).findFirstByTargetIdOrderByStartedAtDesc(anyLong());
    }

    @Test
    void 한_대상의_실행_실패가_다른_대상_처리를_막지_않는다() {
        CollectionTarget failing = target(1L, "HOUR", 12);
        CollectionTarget succeeding = target(2L, "HOUR", 3);
        given(collectionTargetRepository.findByEnabledTrue()).willReturn(List.of(failing, succeeding));
        given(collectionJobRepository.findFirstByTargetIdOrderByStartedAtDesc(1L)).willReturn(Optional.empty());
        given(collectionJobRepository.findFirstByTargetIdOrderByStartedAtDesc(2L)).willReturn(Optional.empty());
        given(collectionExecutionService.execute(eq(1L), isNull(), eq(TriggerType.SCHEDULED)))
                .willThrow(new BusinessException(ErrorCode.COLLECTION_ALREADY_RUNNING));

        collectionScheduler.run();

        verify(collectionExecutionService, times(1)).execute(1L, null, TriggerType.SCHEDULED);
        verify(collectionExecutionService, times(1)).execute(2L, null, TriggerType.SCHEDULED);
    }
}
