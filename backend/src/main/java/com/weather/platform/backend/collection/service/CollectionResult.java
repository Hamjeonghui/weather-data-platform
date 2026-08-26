package com.weather.platform.backend.collection.service;

public record CollectionResult(boolean anySuccess, long receivedCount, long savedCount, long duplicateCount) {

    public static CollectionResult empty() {
        return new CollectionResult(false, 0, 0, 0);
    }
}
