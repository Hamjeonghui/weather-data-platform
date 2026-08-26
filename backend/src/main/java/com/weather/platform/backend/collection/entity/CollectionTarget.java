package com.weather.platform.backend.collection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "collection_target")
public class CollectionTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "data_code", nullable = false)
    private String dataCode;

    @Column(name = "data_name_ko")
    private String dataNameKo;

    @Column(name = "controller_type", nullable = false)
    private String controllerType;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "schedule_type", nullable = false)
    private String scheduleType;

    @Column(name = "interval_value", nullable = false)
    private int intervalValue;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected CollectionTarget() {
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getDataCode() {
        return dataCode;
    }

    public String getDataNameKo() {
        return dataNameKo;
    }

    public String getControllerType() {
        return controllerType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public int getIntervalValue() {
        return intervalValue;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
