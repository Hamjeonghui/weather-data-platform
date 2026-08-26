package com.weather.platform.backend.collection.controller;

import com.weather.platform.backend.collection.dto.CollectionJobDetailResponse;
import com.weather.platform.backend.collection.dto.CollectionJobListResponse;
import com.weather.platform.backend.collection.entity.CollectionStatus;
import com.weather.platform.backend.collection.entity.TriggerType;
import com.weather.platform.backend.collection.service.AdminCollectionService;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/collection-jobs")
public class CollectionJobController {

    private final AdminCollectionService adminCollectionService;

    public CollectionJobController(AdminCollectionService adminCollectionService) {
        this.adminCollectionService = adminCollectionService;
    }

    @GetMapping
    public CollectionJobListResponse getJobs(@RequestParam(required = false) Long targetId,
                                              @RequestParam(required = false) CollectionStatus status,
                                              @RequestParam(required = false) TriggerType triggerType,
                                              @RequestParam(required = false) OffsetDateTime from,
                                              @RequestParam(required = false) OffsetDateTime to,
                                              @PageableDefault(size = 20, sort = "startedAt",
                                                      direction = Sort.Direction.DESC) Pageable pageable) {
        return adminCollectionService.getJobs(targetId, status, triggerType, from, to, pageable);
    }

    @GetMapping("/{jobId}")
    public CollectionJobDetailResponse getJobDetail(@PathVariable Long jobId) {
        return adminCollectionService.getJobDetail(jobId);
    }
}
