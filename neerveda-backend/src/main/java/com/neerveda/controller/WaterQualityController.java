package com.neerveda.controller;

import com.neerveda.dto.ApiResponse;
import com.neerveda.model.WaterQualityData;
import com.neerveda.service.WaterQualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 🌊 WaterQualityController
 *
 * This class handles all API requests related to water quality.
 * Think of it as the "front door" of your backend.
 *
 * BASE URL: http://localhost:8080/api/v1/water
 *
 * ENDPOINTS:
 * POST   /api/v1/water/reading      → Submit new sensor reading
 * GET    /api/v1/water/readings     → Get all readings
 * GET    /api/v1/water/reading/{id} → Get one reading by ID
 * GET    /api/v1/water/status/{villageId} → Get latest status for a village
 * GET    /api/v1/water/health       → Check if API is running
 */
@RestController                          // This class handles HTTP requests and returns JSON
@RequestMapping("/api/v1/water")         // All URLs in this class start with /api/v1/water
@CrossOrigin(origins = "*")              // Allows frontend (React/etc) to call this API
public class WaterQualityController {

    // Spring automatically injects the service we created
    @Autowired
    private WaterQualityService waterQualityService;

    // Temporary in-memory storage (will be replaced with Firebase later)
    private List<WaterQualityData> readings = new ArrayList<>();

    // =========================================================
    // 1. HEALTH CHECK - Test if API is running
    //    URL: GET http://localhost:8080/api/v1/water/health
    // =========================================================
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            ApiResponse.success("💧 NeerVeda Water Quality API is running!", "OK")
        );
    }

    // =========================================================
    // 2. SUBMIT SENSOR READING
    //    URL: POST http://localhost:8080/api/v1/water/reading
    //    Body: JSON with pH, TDS, turbidity, temperature
    //
    //    The ESP32 IoT sensor will call this endpoint
    //    every time it takes a new water reading.
    // =========================================================
    @PostMapping("/reading")
    public ResponseEntity<ApiResponse<WaterQualityData>> submitReading(
            @RequestBody WaterQualityData rawData) {

        try {
            // Analyze the incoming sensor data
            WaterQualityData analyzedData = waterQualityService.analyzeWaterQuality(rawData);

            // Save to our temporary list (Firebase integration comes next)
            readings.add(analyzedData);

            // Log to console for debugging
            System.out.println("📊 New reading received from: " + analyzedData.getVillageName());
            System.out.println("   Status: " + analyzedData.getStatus());
            System.out.println("   pH: " + analyzedData.getPh());
            System.out.println("   TDS: " + analyzedData.getTds() + " ppm");
            System.out.println("   Turbidity: " + analyzedData.getTurbidity() + " NTU");
            System.out.println("   Temperature: " + analyzedData.getTemperature() + "°C");

            // If water is DANGEROUS, log an alert
            if (waterQualityService.requiresImmediateAlert(analyzedData)) {
                System.out.println("🚨 ALERT: " + waterQualityService.generateSmsAlert(analyzedData));
            }

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.created(
                        "Water quality reading recorded successfully. Status: " + analyzedData.getStatus(),
                        analyzedData
                    ));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to process reading: " + e.getMessage(), 500));
        }
    }

    // =========================================================
    // 3. GET ALL READINGS
    //    URL: GET http://localhost:8080/api/v1/water/readings
    //
    //    The dashboard will call this to show all sensor data.
    // =========================================================
    @GetMapping("/readings")
    public ResponseEntity<ApiResponse<List<WaterQualityData>>> getAllReadings() {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Retrieved " + readings.size() + " water quality readings.",
                readings
            )
        );
    }

    // =========================================================
    // 4. GET READING BY ID
    //    URL: GET http://localhost:8080/api/v1/water/reading/{id}
    // =========================================================
    @GetMapping("/reading/{id}")
    public ResponseEntity<ApiResponse<WaterQualityData>> getReadingById(
            @PathVariable String id) {

        return readings.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .map(r -> ResponseEntity.ok(ApiResponse.success("Reading found.", r)))
                .orElse(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Reading with ID " + id + " not found.", 404)));
    }

    // =========================================================
    // 5. GET LATEST STATUS FOR A VILLAGE
    //    URL: GET http://localhost:8080/api/v1/water/status/{villageId}
    //
    //    Dashboard shows per-village water safety status.
    // =========================================================
    @GetMapping("/status/{villageId}")
    public ResponseEntity<ApiResponse<WaterQualityData>> getVillageStatus(
            @PathVariable String villageId) {

        // Find the most recent reading for this village
        return readings.stream()
                .filter(r -> r.getVillageId().equals(villageId))
                .reduce((first, second) -> second) // Get last element
                .map(r -> ResponseEntity.ok(
                    ApiResponse.success("Latest reading for " + r.getVillageName(), r)
                ))
                .orElse(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No readings found for village: " + villageId, 404)));
    }

    // =========================================================
    // 6. GET ONLY DANGEROUS READINGS (for alert dashboard)
    //    URL: GET http://localhost:8080/api/v1/water/alerts
    // =========================================================
    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<WaterQualityData>>> getDangerousReadings() {
        List<WaterQualityData> dangerous = readings.stream()
                .filter(r -> r.getStatus() == WaterQualityData.WaterStatus.DANGER)
                .toList();

        return ResponseEntity.ok(
            ApiResponse.success(
                dangerous.isEmpty()
                    ? "✅ No dangerous water quality readings found."
                    : "🚨 Found " + dangerous.size() + " dangerous readings!",
                dangerous
            )
        );
    }
}
