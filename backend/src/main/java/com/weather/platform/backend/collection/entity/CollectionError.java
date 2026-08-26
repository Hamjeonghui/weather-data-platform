package com.weather.platform.backend.collection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "collection_error")
public class CollectionError {

    @Id
    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "retryable", nullable = false)
    private boolean retryable;

    protected CollectionError() {
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
