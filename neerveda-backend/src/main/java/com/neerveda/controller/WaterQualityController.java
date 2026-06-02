package com.neerveda.controller;

import com.neerveda.dto.ApiResponse;
import com.neerveda.dto.WaterReadingRequest;
import com.neerveda.model.WaterQualityData;
import com.neerveda.service.WaterQualityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 🌊 WaterQualityController
 *
 * BASE URL: /api/v1/water
 *
 * Endpoints:
 *   GET  /health                   — public
 *   POST /reading                  — WATER_INSPECTOR, ADMIN (IoT / device)
 *   GET  /readings                 — authenticated
 *   GET  /reading/{id}             — authenticated
 *   GET  /status/{villageId}       — authenticated
 *   GET  /alerts                   — authenticated
 */
@RestController
@RequestMapping("/api/v1/water")
@RequiredArgsConstructor
@Tag(name = "Water Quality", description = "IoT sensor data ingestion and water quality monitoring")
@SecurityRequirement(name = "bearerAuth")
public class WaterQualityController {

    private final WaterQualityService waterQualityService;

    // -------------------------------------------------------
    // HEALTH
    // -------------------------------------------------------

    @GetMapping("/health")
    @Operation(summary = "API health check", description = "No authentication required")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("💧 NeerVeda Water Quality API is running!", "OK"));
    }

    // -------------------------------------------------------
    // SUBMIT SENSOR READING (IoT)
    // -------------------------------------------------------

    @PostMapping("/reading")
    @PreAuthorize("hasAnyRole('ADMIN', 'WATER_INSPECTOR')")
    @Operation(summary = "Submit IoT sensor reading", description = "Called by ESP32 devices")
    public ResponseEntity<ApiResponse<WaterQualityData>> submitReading(
            @Valid @RequestBody WaterReadingRequest request) {

        WaterQualityData data = mapToData(request);
        WaterQualityData result = waterQualityService.analyzeAndSave(data);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.created(
                "Reading recorded. Status: " + result.getStatus(),
                result
            ));
    }

    // -------------------------------------------------------
    // GET ALL READINGS
    // -------------------------------------------------------

    @GetMapping("/readings")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all water quality readings")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllReadings() {
        List<Map<String, Object>> readings = waterQualityService.getAllReadings();
        return ResponseEntity.ok(ApiResponse.success(
            "Retrieved " + readings.size() + " readings.",
            readings
        ));
    }

    // -------------------------------------------------------
    // GET READING BY ID
    // -------------------------------------------------------

    @GetMapping("/reading/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a specific reading by ID")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReadingById(@PathVariable String id) {
        return waterQualityService.getReadingById(id)
            .map(r -> ResponseEntity.ok(ApiResponse.success("Reading found.", r)))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Reading not found: " + id, 404)));
    }

    // -------------------------------------------------------
    // GET READINGS BY VILLAGE
    // -------------------------------------------------------

    @GetMapping("/village/{villageId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all readings for a village")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getByVillage(
            @PathVariable String villageId) {
        List<Map<String, Object>> readings = waterQualityService.getReadingsByVillage(villageId);
        return ResponseEntity.ok(ApiResponse.success(
            "Found " + readings.size() + " readings for village " + villageId,
            readings
        ));
    }

    // -------------------------------------------------------
    // GET DANGEROUS READINGS
    // -------------------------------------------------------

    @GetMapping("/alerts")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all DANGER-status readings")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDangerousReadings() {
        List<Map<String, Object>> dangerous = waterQualityService.getDangerousReadings();
        return ResponseEntity.ok(ApiResponse.success(
            dangerous.isEmpty()
                ? "No dangerous readings found."
                : "⚠️ Found " + dangerous.size() + " dangerous readings.",
            dangerous
        ));
    }

    // -------------------------------------------------------
    // MAPPING
    // -------------------------------------------------------

    private WaterQualityData mapToData(WaterReadingRequest req) {
        return WaterQualityData.builder()
            .villageId(req.getVillageId())
            .villageName(req.getVillageName())
            .district(req.getDistrict())
            .state(req.getState())
            .latitude(req.getLatitude())
            .longitude(req.getLongitude())
            .ph(req.getPh())
            .tds(req.getTds())
            .turbidity(req.getTurbidity())
            .temperature(req.getTemperature())
            .deviceId(req.getDeviceId())
            .build();
    }
}
