package com.neerveda.controller;

import com.neerveda.dto.ApiResponse;
import com.neerveda.security.NeerVedaUserPrincipal;
import com.neerveda.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 🚨 AlertController
 *
 * BASE URL: /api/v1/alerts
 */
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Alert management — view, acknowledge, resolve")
@SecurityRequirement(name = "bearerAuth")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GOVERNMENT_OFFICER', 'HEALTH_WORKER')")
    @Operation(summary = "Get all alerts")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllAlerts() {
        List<Map<String, Object>> alerts = alertService.getAllAlerts();
        return ResponseEntity.ok(ApiResponse.success("Retrieved " + alerts.size() + " alerts.", alerts));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOVERNMENT_OFFICER', 'HEALTH_WORKER')")
    @Operation(summary = "Get active alerts only")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActiveAlerts() {
        List<Map<String, Object>> alerts = alertService.getActiveAlerts();
        return ResponseEntity.ok(ApiResponse.success(
            alerts.isEmpty() ? "No active alerts." : alerts.size() + " active alert(s).",
            alerts
        ));
    }

    @GetMapping("/village/{villageId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get alerts for a specific village")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAlertsByVillage(
            @PathVariable String villageId) {
        List<Map<String, Object>> alerts = alertService.getAlertsByVillage(villageId);
        return ResponseEntity.ok(ApiResponse.success(
            "Found " + alerts.size() + " alerts for village " + villageId, alerts));
    }

    @PutMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOVERNMENT_OFFICER', 'HEALTH_WORKER')")
    @Operation(summary = "Acknowledge an alert")
    public ResponseEntity<ApiResponse<String>> acknowledgeAlert(
            @PathVariable String id,
            @AuthenticationPrincipal NeerVedaUserPrincipal principal) {

        String user = principal != null ? principal.getUsername() : "system";
        alertService.acknowledgeAlert(id, user);
        return ResponseEntity.ok(ApiResponse.success("Alert acknowledged.", "ACKNOWLEDGED"));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOVERNMENT_OFFICER')")
    @Operation(summary = "Resolve an alert")
    public ResponseEntity<ApiResponse<String>> resolveAlert(
            @PathVariable String id,
            @AuthenticationPrincipal NeerVedaUserPrincipal principal) {

        String user = principal != null ? principal.getUsername() : "system";
        alertService.resolveAlert(id, user);
        return ResponseEntity.ok(ApiResponse.success("Alert resolved.", "RESOLVED"));
    }
}
