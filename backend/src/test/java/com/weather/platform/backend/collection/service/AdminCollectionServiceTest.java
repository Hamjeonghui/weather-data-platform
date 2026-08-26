package com.weather.platform.backend.collection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.weather.platform.backend.collection.dto.CollectionDashboardSummaryResponse;
import com.weather.platform.backend.collection.dto.CollectionJobDetailResponse;
import com.weather.platform.backend.collection.dto.CollectionJobListResponse;
import com.weather.platform.backend.collection.dto.CollectionTargetResponse;
import com.weather.platform.backend.collection.dto.UpdateCollectionTargetRequest;
import com.weather.platform.backend.collection.entity.CollectionError;
import com.weather.platform.backend.collection.entity.CollectionJob;
import com.weather.platform.backend.collection.entity.CollectionStatus;
import com.weather.platform.backend.collection.entity.CollectionTarget;
import com.weather.platform.backend.collection.entity.TriggerType;
import com.weather.platform.backend.collection.repository.CollectionErrorRepository;
import com.weather.platform.backend.collection.repository.CollectionJobRepository;
import com.weather.platform.backend.collection.repository.CollectionTargetRepository;
import com.weather.platform.backend.global.exception.BusinessException;
import com.weather.platform.backend.global.exception.ErrorCode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AdminCollectionServiceTest {

    private static final ZoneOffset SEOUL_OFFSET = ZoneOffset.of("+09:00");

    @Mock
    private CollectionTargetRepository collectionTargetRepository;

    @Mock
    private CollectionJobRepository collectionJobRepository;

    @Mock
    private CollectionErrorRepository collectionErrorRepository;

    private AdminCollectionService adminCollectionService;

    private AdminCollectionService service() {
        return new AdminCollectionService(collectionTargetRepository, collectionJobRepository,
                collectionErrorRepository);
    }

    private CollectionTarget target(Long id, String dataCode, boolean enabled, String scheduleType, int intervalValue) {
        CollectionTarget target = mock(CollectionTarget.class);
        org.mockito.Mockito.lenient().when(target.getTargetId()).thenReturn(id);
        org.mockito.Mockito.lenient().when(target.getDataCode()).thenReturn(dataCode);
        org.mockito.Mockito.lenient().when(target.getDataNameKo()).thenReturn("중기예보");
        org.mockito.Mockito.lenient().when(target.isEnabled()).thenReturn(enabled);
        org.mockito.Mockito.lenient().when(target.getScheduleType()).thenReturn(scheduleType);
        org.mockito.Mockito.lenient().when(target.getIntervalValue()).thenReturn(intervalValue);
        return target;
    }

    @Test
    void 대상_조회시_이전_실행이력이_없으면_다음실행시각은_즉시이고_최근상태는_null이다() {
        adminCollectionService = service();
        CollectionTarget target = target(1L, "MID_FORECAST", true, "HOUR", 12);
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.of(target));
        given(collectionJobRepository.findFirstByTargetIdOrderByStartedAtDesc(1L)).willReturn(Optional.empty());

        CollectionTargetResponse response = adminCollectionService.getTarget(1L);

        assertThat(response.lastExecutedAt()).isNull();
        assertThat(response.latestStatus()).isNull();
        assertThat(response.nextExecutedAt()).isNotNull();
    }

    @Test
    void 대상_조회시_이전_실행이력이_있으면_간격만큼_더한_다음실행시각을_반환한다() {
        adminCollectionService = service();
        CollectionTarget target = target(1L, "MID_FORECAST", true, "HOUR", 12);
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.of(target));
        OffsetDateTime startedAt = OffsetDateTime.now(SEOUL_OFFSET).minusHours(1);
        CollectionJob lastJob = mock(CollectionJob.class);
        given(lastJob.getStartedAt()).willReturn(startedAt);
        given(lastJob.getStatus()).willReturn(CollectionStatus.SUCCESS);
        given(collectionJobRepository.findFirstByTargetIdOrderByStartedAtDesc(1L)).willReturn(Optional.of(lastJob));

        CollectionTargetResponse response = adminCollectionService.getTarget(1L);

        assertThat(response.lastExecutedAt()).isEqualTo(startedAt);
        assertThat(response.latestStatus()).isEqualTo("SUCCESS");
        assertThat(response.nextExecutedAt()).isEqualTo(startedAt.plusHours(12));
    }

    @Test
    void 비활성_대상은_다음실행시각이_null이다() {
        adminCollectionService = service();
        CollectionTarget target = target(1L, "MID_FORECAST", false, "HOUR", 12);
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.of(target));
        given(collectionJobRepository.findFirstByTargetIdOrderByStartedAtDesc(1L)).willReturn(Optional.empty());

        CollectionTargetResponse response = adminCollectionService.getTarget(1L);

        assertThat(response.nextExecutedAt()).isNull();
    }

    @Test
    void 존재하지_않는_대상을_조회하면_COLLECTION_TARGET_NOT_FOUND_예외가_발생한다() {
        adminCollectionService = service();
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminCollectionService.getTarget(1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.COLLECTION_TARGET_NOT_FOUND);
    }

    @Test
    void 지원하지_않는_수행단위로_설정을_변경하면_INVALID_REQUEST_예외가_발생한다() {
        adminCollectionService = service();
        CollectionTarget target = target(1L, "MID_FORECAST", true, "HOUR", 12);
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.of(target));

        UpdateCollectionTargetRequest request = new UpdateCollectionTargetRequest(true, "WEEK", 1, null);

        assertThatThrownBy(() -> adminCollectionService.updateTarget(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void 설정_변경에_성공하면_대상의_스케줄이_갱신된다() {
        adminCollectionService = service();
        CollectionTarget target = target(1L, "MID_FORECAST", true, "HOUR", 12);
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.of(target));
        given(collectionJobRepository.findFirstByTargetIdOrderByStartedAtDesc(1L)).willReturn(Optional.empty());

        UpdateCollectionTargetRequest request = new UpdateCollectionTargetRequest(false, "DAY", 1, null);
        adminCollectionService.updateTarget(1L, request);

        verify(target).updateSchedule(eq(false), eq("DAY"), eq(1), any());
    }

    @Test
    void 대시보드_요약은_기간내_성공_실패_건수와_현재_실행중_건수를_모두_담는다() {
        adminCollectionService = service();
        given(collectionTargetRepository.count()).willReturn(3L);
        given(collectionTargetRepository.countByEnabledTrue()).willReturn(2L);
        given(collectionJobRepository.countByStatus(CollectionStatus.RUNNING)).willReturn(1L);
        given(collectionJobRepository.countByStatusAndStartedAtBetween(
                eq(CollectionStatus.SUCCESS), any(), any())).willReturn(10L);
        given(collectionJobRepository.countByStatusAndStartedAtBetween(
                eq(CollectionStatus.FAILED), any(), any())).willReturn(2L);
        given(collectionJobRepository.findLatestFinishedAt(
                eq(CollectionStatus.SUCCESS), any(), any()))
                .willReturn(Optional.empty());

        CollectionDashboardSummaryResponse response =
                adminCollectionService.getDashboardSummary(LocalDate.now(), LocalDate.now());

        assertThat(response.targetCount()).isEqualTo(3L);
        assertThat(response.enabledTargetCount()).isEqualTo(2L);
        assertThat(response.runningCount()).isEqualTo(1L);
        assertThat(response.successCount()).isEqualTo(10L);
        assertThat(response.failedCount()).isEqualTo(2L);
    }

    @Test
    void 이력_목록은_job에_기록된_수집_건수를_그대로_반환한다() {
        adminCollectionService = service();
        CollectionTarget target = target(1L, "MID_FORECAST", true, "HOUR", 12);
        CollectionJob job = mock(CollectionJob.class);
        given(job.getJobId()).willReturn(100L);
        given(job.getTargetId()).willReturn(1L);
        given(job.getStatus()).willReturn(CollectionStatus.SUCCESS);
        given(job.getTriggerType()).willReturn(TriggerType.SCHEDULED);
        given(job.getStartedAt()).willReturn(OffsetDateTime.now(SEOUL_OFFSET));
        given(job.getReceivedCount()).willReturn(176L);
        given(job.getSavedCount()).willReturn(173L);
        given(job.getDuplicateCount()).willReturn(3L);

        Pageable pageable = PageRequest.of(0, 20);
        given(collectionJobRepository.search(any(), any(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(job), pageable, 1));
        given(collectionTargetRepository.findAllById(any())).willReturn(List.of(target));

        CollectionJobListResponse response = adminCollectionService.getJobs(
                null, null, null, null, null, pageable);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).receivedCount()).isEqualTo(176L);
        assertThat(response.items().get(0).savedCount()).isEqualTo(173L);
        assertThat(response.items().get(0).duplicateCount()).isEqualTo(3L);
    }

    @Test
    void 이력_상세는_에러메시지와_재시도가능여부를_에러코드로_조회한다() {
        adminCollectionService = service();
        CollectionTarget target = target(1L, "MID_FORECAST", true, "HOUR", 12);
        CollectionJob job = mock(CollectionJob.class);
        given(job.getJobId()).willReturn(100L);
        given(job.getTargetId()).willReturn(1L);
        given(job.getStatus()).willReturn(CollectionStatus.FAILED);
        given(job.getTriggerType()).willReturn(TriggerType.SCHEDULED);
        given(job.getStartedAt()).willReturn(OffsetDateTime.now(SEOUL_OFFSET));
        given(job.getErrorCode()).willReturn("EXTERNAL_API_ERROR");
        given(job.getRetryOfJobId()).willReturn(99L);
        given(job.getReceivedCount()).willReturn(0L);
        given(job.getSavedCount()).willReturn(0L);
        given(job.getDuplicateCount()).willReturn(0L);

        CollectionError error = mock(CollectionError.class);
        given(error.getErrorMessage()).willReturn("외부 API 호출 중 오류가 발생했습니다.");
        given(error.isRetryable()).willReturn(true);

        given(collectionJobRepository.findById(100L)).willReturn(Optional.of(job));
        given(collectionTargetRepository.findById(1L)).willReturn(Optional.of(target));
        given(collectionErrorRepository.findById("EXTERNAL_API_ERROR")).willReturn(Optional.of(error));

        CollectionJobDetailResponse response = adminCollectionService.getJobDetail(100L);

        assertThat(response.errorMessage()).isEqualTo("외부 API 호출 중 오류가 발생했습니다.");
        assertThat(response.retryable()).isTrue();
        assertThat(response.retryOfJobId()).isEqualTo(99L);
    }

    @Test
    void 존재하지_않는_작업번호를_조회하면_COLLECTION_JOB_NOT_FOUND_예외가_발생한다() {
        adminCollectionService = service();
        given(collectionJobRepository.findById(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminCollectionService.getJobDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.COLLECTION_JOB_NOT_FOUND);
    }
}
