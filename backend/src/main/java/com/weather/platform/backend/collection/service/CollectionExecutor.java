package com.weather.platform.backend.collection.service;

import com.weather.platform.backend.collection.entity.CollectionJob;
import com.weather.platform.backend.collection.entity.CollectionTarget;

public interface CollectionExecutor {

    String supportedDataCode();

    boolean collect(CollectionTarget target, CollectionJob job);
}
