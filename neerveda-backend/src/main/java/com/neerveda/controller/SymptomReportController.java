package com.neerveda.controller;

import com.neerveda.dto.ApiResponse;
import com.neerveda.model.SymptomReport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 🏥 SymptomReportController
 *
 * Handles disease symptom reports submitted by:
 * - ASHA Workers (via mobile app)
 * - Volunteers
 * - Clinics
 * - Community members (via SMS)
 *
 * BASE URL: http://localhost:8080/api/v1/symptoms
 *
 * ENDPOINTS:
 * POST  /api/v1/symptoms/report         → Submit new symptom report
 * GET   /api/v1/symptoms/reports        → Get all reports
 * GET   /api/v1/symptoms/reports/{villageId} → Get reports by village
 * PUT   /api/v1/symptoms/report/{id}/review  → Health officer reviews report
 */
@RestController
@RequestMapping("/api/v1/symptoms")
@CrossOrigin(origins = "*")
public class SymptomReportController {

    // Temporary storage (Firebase integration coming next)
    private List<SymptomReport> reports = new ArrayList<>();

    // =========================================================
    // 1. SUBMIT SYMPTOM REPORT
    //    URL: POST http://localhost:8080/api/v1/symptoms/report
    // =========================================================
    @PostMapping("/report")
    public ResponseEntity<ApiResponse<SymptomReport>> submitReport(
            @RequestBody SymptomReport report) {

        try {
            // Set ID and timestamp
            report.setId(UUID.randomUUID().toString());
            report.setTimestamp(LocalDateTime.now());
            report.setStatus("PENDING");

            reports.add(report);

            System.out.println("🏥 New symptom report from: " + report.getReportedBy());
            System.out.println("   Village: " + report.getVillageName());
            System.out.println("   Symptoms: " + report.getSymptoms());
            System.out.println("   Suspected Disease: " + report.getSuspectedDisease());
            System.out.println("   Affected Count: " + report.getAffectedCount());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.created("Symptom report submitted successfully.", report));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to submit report: " + e.getMessage(), 500));
        }
    }

    // =========================================================
    // 2. GET ALL REPORTS
    //    URL: GET http://localhost:8080/api/v1/symptoms/reports
    // =========================================================
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<SymptomReport>>> getAllReports() {
        return ResponseEntity.ok(
            ApiResponse.success("Retrieved " + reports.size() + " symptom reports.", reports)
        );
    }

    // =========================================================
    // 3. GET REPORTS BY VILLAGE
    //    URL: GET http://localhost:8080/api/v1/symptoms/reports/{villageId}
    // =========================================================
    @GetMapping("/reports/{villageId}")
    public ResponseEntity<ApiResponse<List<SymptomReport>>> getReportsByVillage(
            @PathVariable String villageId) {

        List<SymptomReport> villageReports = reports.stream()
                .filter(r -> villageId.equals(r.getVillageId()))
                .toList();

        return ResponseEntity.ok(
            ApiResponse.success(
                "Found " + villageReports.size() + " reports for village " + villageId,
                villageReports
            )
        );
    }

    // =========================================================
    // 4. REVIEW A REPORT (Health Officer)
    //    URL: PUT http://localhost:8080/api/v1/symptoms/report/{id}/review
    // =========================================================
    @PutMapping("/report/{id}/review")
    public ResponseEntity<ApiResponse<SymptomReport>> reviewReport(
            @PathVariable String id,
            @RequestParam String status,
            @RequestParam(required = false) String notes) {

        return reports.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .map(r -> {
                    r.setStatus(status);
                    r.setReviewNotes(notes);
                    return ResponseEntity.ok(
                        ApiResponse.success("Report reviewed. Status: " + status, r)
                    );
                })
                .orElse(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Report not found: " + id, 404)));
    }
}
