package com.weather.platform.backend.collection.service;

import com.weather.platform.backend.collection.dto.CollectionDashboardSummaryResponse;
import com.weather.platform.backend.collection.dto.CollectionJobDetailResponse;
import com.weather.platform.backend.collection.dto.CollectionJobListResponse;
import com.weather.platform.backend.collection.dto.CollectionJobSummaryResponse;
import com.weather.platform.backend.collection.dto.CollectionTargetListResponse;
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
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminCollectionService {

    private static final ZoneOffset SEOUL_OFFSET = ZoneOffset.of("+09:00");
    private static final Set<String> VALID_SCHEDULE_TYPES = Set.of("MINUTE", "HOUR", "DAY");

    private final CollectionTargetRepository collectionTargetRepository;
    private final CollectionJobRepository collectionJobRepository;
    private final CollectionErrorRepository collectionErrorRepository;

    public AdminCollectionService(CollectionTargetRepository collectionTargetRepository,
                                   CollectionJobRepository collectionJobRepository,
                                   CollectionErrorRepository collectionErrorRepository) {
        this.collectionTargetRepository = collectionTargetRepository;
        this.collectionJobRepository = collectionJobRepository;
        this.collectionErrorRepository = collectionErrorRepository;
    }

    public CollectionTargetListResponse getTargets() {
        List<CollectionTargetResponse> items = collectionTargetRepository.findAll().stream()
                .map(this::toTargetResponse)
                .toList();
        return new CollectionTargetListResponse(items);
    }

    public CollectionTargetResponse getTarget(Long targetId) {
        CollectionTarget target = collectionTargetRepository.findById(targetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_TARGET_NOT_FOUND));
        return toTargetResponse(target);
    }

    @Transactional
    public CollectionTargetResponse updateTarget(Long targetId, UpdateCollectionTargetRequest request) {
        CollectionTarget target = collectionTargetRepository.findById(targetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_TARGET_NOT_FOUND));

        if (!VALID_SCHEDULE_TYPES.contains(request.scheduleType())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "지원하지 않는 수행 단위입니다: " + request.scheduleType());
        }

        target.updateSchedule(request.enabled(), request.scheduleType(), request.intervalValue(),
                OffsetDateTime.now(SEOUL_OFFSET));
        return toTargetResponse(target);
    }

    public CollectionDashboardSummaryResponse getDashboardSummary(LocalDate from, LocalDate to) {
        OffsetDateTime fromAt = (from == null ? LocalDate.now(SEOUL_OFFSET) : from).atStartOfDay(SEOUL_OFFSET).toOffsetDateTime();
        OffsetDateTime toAt = to == null
                ? OffsetDateTime.now(SEOUL_OFFSET)
                : to.plusDays(1).atStartOfDay(SEOUL_OFFSET).toOffsetDateTime();

        long targetCount = collectionTargetRepository.count();
        long enabledTargetCount = collectionTargetRepository.countByEnabledTrue();
        long runningCount = collectionJobRepository.countByStatus(CollectionStatus.RUNNING);
        long successCount = collectionJobRepository.countByStatusAndStartedAtBetween(CollectionStatus.SUCCESS, fromAt, toAt);
        long failedCount = collectionJobRepository.countByStatusAndStartedAtBetween(CollectionStatus.FAILED, fromAt, toAt);
        OffsetDateTime latestCollectedAt = collectionJobRepository
                .findLatestFinishedAt(CollectionStatus.SUCCESS, fromAt, toAt)
                .orElse(null);

        return new CollectionDashboardSummaryResponse(
                targetCount, enabledTargetCount, runningCount, successCount, failedCount, latestCollectedAt);
    }

    public CollectionJobListResponse getJobs(Long targetId, CollectionStatus status, TriggerType triggerType,
                                              OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        OffsetDateTime fromAt = from == null
                ? LocalDate.now(SEOUL_OFFSET).atStartOfDay(SEOUL_OFFSET).toOffsetDateTime()
                : from;
        OffsetDateTime toAt = to == null ? OffsetDateTime.now(SEOUL_OFFSET) : to;

        Page<CollectionJob> page = collectionJobRepository.search(targetId, status, triggerType, fromAt, toAt, pageable);

        Map<Long, CollectionTarget> targetsById = collectionTargetRepository.findAllById(
                        page.getContent().stream().map(CollectionJob::getTargetId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(CollectionTarget::getTargetId, target -> target));

        List<CollectionJobSummaryResponse> items = page.getContent().stream()
                .map(job -> {
                    CollectionTarget target = targetsById.get(job.getTargetId());
                    return new CollectionJobSummaryResponse(
                            job.getJobId(), job.getTargetId(), target == null ? null : target.getDataNameKo(),
                            job.getStatus().name(), job.getTriggerType().name(),
                            job.getStartedAt(), job.getFinishedAt(),
                            job.getReceivedCount(), job.getSavedCount(), job.getDuplicateCount(), job.getErrorCode());
                })
                .toList();

        return new CollectionJobListResponse(items, page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast());
    }

    public CollectionJobDetailResponse getJobDetail(Long jobId) {
        CollectionJob job = collectionJobRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_JOB_NOT_FOUND));
        CollectionTarget target = collectionTargetRepository.findById(job.getTargetId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_TARGET_NOT_FOUND));

        String errorMessage = null;
        Boolean retryable = null;
        if (job.getErrorCode() != null) {
            Optional<CollectionError> error = collectionErrorRepository.findById(job.getErrorCode());
            errorMessage = error.map(CollectionError::getErrorMessage).orElse(null);
            retryable = error.map(CollectionError::isRetryable).orElse(null);
        }

        return new CollectionJobDetailResponse(
                job.getJobId(), target.getDataNameKo(), job.getStatus().name(), job.getTriggerType().name(),
                job.getStartedAt(), job.getFinishedAt(),
                job.getReceivedCount(), job.getSavedCount(), job.getDuplicateCount(),
                job.getErrorCode(), errorMessage, retryable, job.getRetryOfJobId());
    }

    private CollectionTargetResponse toTargetResponse(CollectionTarget target) {
        Optional<CollectionJob> lastJob =
                collectionJobRepository.findFirstByTargetIdOrderByStartedAtDesc(target.getTargetId());

        OffsetDateTime lastExecutedAt = lastJob.map(CollectionJob::getStartedAt).orElse(null);
        String latestStatus = lastJob.map(job -> job.getStatus().name()).orElse(null);
        OffsetDateTime nextExecutedAt = calculateNextExecutedAt(target, lastJob);

        return new CollectionTargetResponse(target.getTargetId(), target.getDataCode(), target.getDataNameKo(),
                target.isEnabled(), target.getScheduleType(), target.getIntervalValue(),
                lastExecutedAt, nextExecutedAt, latestStatus);
    }

    private OffsetDateTime calculateNextExecutedAt(CollectionTarget target, Optional<CollectionJob> lastJob) {
        if (!target.isEnabled()) {
            return null;
        }
        if (lastJob.isEmpty()) {
            return OffsetDateTime.now(SEOUL_OFFSET);
        }
        Duration interval = ScheduleIntervalCalculator.toDuration(target.getScheduleType(), target.getIntervalValue());
        return lastJob.get().getStartedAt().plus(interval);
    }
}
