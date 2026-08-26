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

    protected CollectionJob() {
    }

    public CollectionJob(Long targetId, CollectionStatus status, TriggerType triggerType,
                          OffsetDateTime startedAt, String executedBy) {
        this.targetId = targetId;
        this.status = status;
        this.triggerType = triggerType;
        this.startedAt = startedAt;
        this.executedBy = executedBy;
    }

    public void complete(CollectionStatus status, OffsetDateTime finishedAt, String errorCode) {
        this.status = status;
        this.finishedAt = finishedAt;
        this.errorCode = errorCode;
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
}
