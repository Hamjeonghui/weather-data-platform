package com.weather.platform.backend.collection.controller;

import com.weather.platform.backend.collection.dto.CollectionTargetListResponse;
import com.weather.platform.backend.collection.dto.CollectionTargetResponse;
import com.weather.platform.backend.collection.dto.ExecuteCollectionResponse;
import com.weather.platform.backend.collection.dto.UpdateCollectionTargetRequest;
import com.weather.platform.backend.collection.service.AdminCollectionService;
import com.weather.platform.backend.collection.service.CollectionExecutionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/collection-targets")
public class CollectionController {

    private final CollectionExecutionService collectionExecutionService;
    private final AdminCollectionService adminCollectionService;

    public CollectionController(CollectionExecutionService collectionExecutionService,
                                 AdminCollectionService adminCollectionService) {
        this.collectionExecutionService = collectionExecutionService;
        this.adminCollectionService = adminCollectionService;
    }

    @GetMapping
    public CollectionTargetListResponse getTargets() {
        return adminCollectionService.getTargets();
    }

    @GetMapping("/{targetId}")
    public CollectionTargetResponse getTarget(@PathVariable Long targetId) {
        return adminCollectionService.getTarget(targetId);
    }

    @PatchMapping("/{targetId}")
    public CollectionTargetResponse updateTarget(@PathVariable Long targetId,
                                                  @Valid @RequestBody UpdateCollectionTargetRequest request) {
        return adminCollectionService.updateTarget(targetId, request);
    }

    @PostMapping("/{targetId}/execute")
    public ResponseEntity<ExecuteCollectionResponse> execute(@PathVariable Long targetId,
                                                              @AuthenticationPrincipal String loginId) {
        ExecuteCollectionResponse response = collectionExecutionService.execute(targetId, loginId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
