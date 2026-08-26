package com.weather.platform.backend.collection.service;

import com.weather.platform.backend.collection.entity.CollectionJob;
import com.weather.platform.backend.collection.entity.CollectionTarget;

public interface CollectionExecutor {

    String supportedDataCode();

    /**
     * @param cyclesBack 0이면 최신 유효 발표시각, 1이면 그보다 한 주기 이전 발표시각으로 수집한다.
     */
    CollectionResult collect(CollectionTarget target, CollectionJob job, int cyclesBack);
}
