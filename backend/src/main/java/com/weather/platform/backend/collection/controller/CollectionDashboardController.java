package com.weather.platform.backend.collection.controller;

import com.weather.platform.backend.collection.dto.CollectionDashboardSummaryResponse;
import com.weather.platform.backend.collection.service.AdminCollectionService;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/collection-dashboard")
public class CollectionDashboardController {

    private final AdminCollectionService adminCollectionService;

    public CollectionDashboardController(AdminCollectionService adminCollectionService) {
        this.adminCollectionService = adminCollectionService;
    }

    @GetMapping("/summary")
    public CollectionDashboardSummaryResponse getSummary(@RequestParam(required = false) LocalDate from,
                                                           @RequestParam(required = false) LocalDate to) {
        return adminCollectionService.getDashboardSummary(from, to);
    }
}
