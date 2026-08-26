package com.weather.platform.backend.collection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "collection_job")
public class CollectionJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CollectionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private TriggerType triggerType;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "executed_by")
    private String executedBy;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "retry_of_job_id")
    private Long retryOfJobId;

    @Column(name = "received_count")
    private Long receivedCount;

    @Column(name = "saved_count")
    private Long savedCount;

    @Column(name = "duplicate_count")
    private Long duplicateCount;

    protected CollectionJob() {
    }

    public CollectionJob(Long targetId, CollectionStatus status, TriggerType triggerType,
                          OffsetDateTime startedAt, String executedBy) {
        this(targetId, status, triggerType, startedAt, executedBy, null);
    }

    public CollectionJob(Long targetId, CollectionStatus status, TriggerType triggerType,
                          OffsetDateTime startedAt, String executedBy, Long retryOfJobId) {
        this.targetId = targetId;
        this.status = status;
        this.triggerType = triggerType;
        this.startedAt = startedAt;
        this.executedBy = executedBy;
        this.retryOfJobId = retryOfJobId;
    }

    public void complete(CollectionStatus status, OffsetDateTime finishedAt, String errorCode) {
        this.status = status;
        this.finishedAt = finishedAt;
        this.errorCode = errorCode;
    }

    public void recordCounts(long receivedCount, long savedCount, long duplicateCount) {
        this.receivedCount = receivedCount;
        this.savedCount = savedCount;
        this.duplicateCount = duplicateCount;
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public CollectionStatus getStatus() {
        return status;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getFinishedAt() {
        return finishedAt;
    }

    public String getExecutedBy() {
        return executedBy;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Long getRetryOfJobId() {
        return retryOfJobId;
    }

    public Long getReceivedCount() {
        return receivedCount;
    }

    public Long getSavedCount() {
        return savedCount;
    }

    public Long getDuplicateCount() {
        return duplicateCount;
    }
}
