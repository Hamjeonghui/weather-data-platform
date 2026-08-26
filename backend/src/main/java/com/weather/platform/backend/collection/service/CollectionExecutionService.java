package com.weather.platform.backend.collection.service;

import com.weather.platform.backend.collection.dto.ExecuteCollectionResponse;
import com.weather.platform.backend.collection.entity.CollectionJob;
import com.weather.platform.backend.collection.entity.CollectionStatus;
import com.weather.platform.backend.collection.entity.CollectionTarget;
import com.weather.platform.backend.collection.entity.TriggerType;
import com.weather.platform.backend.collection.repository.CollectionJobRepository;
import com.weather.platform.backend.collection.repository.CollectionTargetRepository;
import com.weather.platform.backend.global.exception.BusinessException;
import com.weather.platform.backend.global.exception.ErrorCode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollectionExecutionService {

    private static final ZoneOffset SEOUL_OFFSET = ZoneOffset.of("+09:00");

    private final CollectionTargetRepository collectionTargetRepository;
    private final CollectionJobRepository collectionJobRepository;
    private final Map<String, CollectionExecutor> executorsByDataCode;

    public CollectionExecutionService(CollectionTargetRepository collectionTargetRepository,
                                       CollectionJobRepository collectionJobRepository,
                                       List<CollectionExecutor> executors) {
        this.collectionTargetRepository = collectionTargetRepository;
        this.collectionJobRepository = collectionJobRepository;
        this.executorsByDataCode = executors.stream()
                .collect(Collectors.toMap(CollectionExecutor::supportedDataCode, Function.identity()));
    }

    @Transactional
    public ExecuteCollectionResponse execute(Long targetId, String executedBy) {
        return execute(targetId, executedBy, TriggerType.MANUAL);
    }

    @Transactional
    public ExecuteCollectionResponse execute(Long targetId, String executedBy, TriggerType triggerType) {
        CollectionTarget target = collectionTargetRepository.findById(targetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_TARGET_NOT_FOUND));

        if (collectionJobRepository.existsByTargetIdAndStatus(targetId, CollectionStatus.RUNNING)) {
            throw new BusinessException(ErrorCode.COLLECTION_ALREADY_RUNNING);
        }

        CollectionExecutor executor = executorsByDataCode.get(target.getDataCode());
        if (executor == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "지원하지 않는 수집 종류입니다: " + target.getDataCode());
        }

        CollectionJob job = collectionJobRepository.save(
                new CollectionJob(targetId, CollectionStatus.RUNNING, triggerType,
                        OffsetDateTime.now(SEOUL_OFFSET), executedBy));

        CollectionResult result = executor.collect(target, job, 0);
        job.recordCounts(result.receivedCount(), result.savedCount(), result.duplicateCount());
        job.complete(result.anySuccess() ? CollectionStatus.SUCCESS : CollectionStatus.FAILED,
                OffsetDateTime.now(SEOUL_OFFSET), result.anySuccess() ? null : ErrorCode.EXTERNAL_API_ERROR.name());

        if (result.anySuccess()) {
            return new ExecuteCollectionResponse(job.getJobId(), job.getStatus().name());
        }

        CollectionJob retryJob = collectionJobRepository.save(
                new CollectionJob(targetId, CollectionStatus.RUNNING, triggerType,
                        OffsetDateTime.now(SEOUL_OFFSET), executedBy, job.getJobId()));

        CollectionResult retryResult = executor.collect(target, retryJob, 1);
        retryJob.recordCounts(retryResult.receivedCount(), retryResult.savedCount(), retryResult.duplicateCount());
        retryJob.complete(retryResult.anySuccess() ? CollectionStatus.SUCCESS : CollectionStatus.FAILED,
                OffsetDateTime.now(SEOUL_OFFSET), retryResult.anySuccess() ? null : ErrorCode.EXTERNAL_API_ERROR.name());

        return new ExecuteCollectionResponse(retryJob.getJobId(), retryJob.getStatus().name());
    }
}
