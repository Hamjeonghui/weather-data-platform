package com.weather.platform.backend.collection.dto;

import java.util.List;

public record CollectionJobListResponse(List<CollectionJobSummaryResponse> items,
                                         int page,
                                         int size,
                                         long totalElements,
                                         int totalPages,
                                         boolean first,
                                         boolean last) {
}
