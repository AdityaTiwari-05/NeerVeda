package com.neerveda.controller;

import com.neerveda.dto.ApiResponse;
import com.neerveda.dto.DashboardStatsResponse;
import com.neerveda.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 📊 DashboardController
 *
 * Aggregated dashboard statistics endpoint.
 * BASE URL: /api/v1/dashboard
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregated dashboard statistics for admin and officer views")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOVERNMENT_OFFICER')")
    @Operation(summary = "Get admin dashboard statistics",
        description = "Returns village counts, alert stats, safety index, and trend charts")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        DashboardStatsResponse stats = dashboardService.getAdminStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics loaded.", stats));
    }
}
