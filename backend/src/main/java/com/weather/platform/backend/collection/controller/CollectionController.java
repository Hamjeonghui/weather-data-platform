package com.weather.platform.backend.collection.controller;

import com.weather.platform.backend.collection.dto.ExecuteCollectionResponse;
import com.weather.platform.backend.collection.service.MidForecastCollectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/collection-targets")
public class CollectionController {

    private final MidForecastCollectionService midForecastCollectionService;

    public CollectionController(MidForecastCollectionService midForecastCollectionService) {
        this.midForecastCollectionService = midForecastCollectionService;
    }

    @PostMapping("/{targetId}/execute")
    public ResponseEntity<ExecuteCollectionResponse> execute(@PathVariable Long targetId,
                                                              @AuthenticationPrincipal String loginId) {
        ExecuteCollectionResponse response = midForecastCollectionService.execute(targetId, loginId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
