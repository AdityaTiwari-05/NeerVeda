package com.neerveda.controller;

import com.neerveda.dto.ApiResponse;
import com.neerveda.model.SymptomReport;
import com.neerveda.security.NeerVedaUserPrincipal;
import com.neerveda.service.SymptomReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 🏥 SymptomReportController
 *
 * BASE URL: /api/v1/symptoms
 *
 * Endpoints:
 *   POST /report                         — HEALTH_WORKER, ADMIN
 *   GET  /reports                        — HEALTH_WORKER, GOVERNMENT_OFFICER, ADMIN
 *   GET  /reports/village/{villageId}    — HEALTH_WORKER, GOVERNMENT_OFFICER, ADMIN
 *   GET  /report/{id}                    — HEALTH_WORKER, GOVERNMENT_OFFICER, ADMIN
 *   PUT  /report/{id}/review             — HEALTH_WORKER, ADMIN
 */
@RestController
@RequestMapping("/api/v1/symptoms")
@RequiredArgsConstructor
@Tag(name = "Symptom Reports", description = "Disease symptom reporting and outbreak monitoring")
@SecurityRequirement(name = "bearerAuth")
public class SymptomReportController {

    private final SymptomReportService symptomReportService;

    // -------------------------------------------------------
    // SUBMIT REPORT
    // -------------------------------------------------------

    @PostMapping("/report")
    @PreAuthorize("hasAnyRole('ADMIN', 'HEALTH_WORKER', 'GOVERNMENT_OFFICER')")
    @Operation(summary = "Submit a disease symptom report")
    public ResponseEntity<ApiResponse<SymptomReport>> submitReport(
            @RequestBody SymptomReport report) {

        SymptomReport saved = symptomReportService.submit(report);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.created("Symptom report submitted successfully.", saved));
    }

    // -------------------------------------------------------
    // GET ALL REPORTS
    // -------------------------------------------------------

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('ADMIN', 'HEALTH_WORKER', 'GOVERNMENT_OFFICER')")
    @Operation(summary = "Get all symptom reports")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllReports() {
        List<Map<String, Object>> reports = symptomReportService.getAll();
        return ResponseEntity.ok(ApiResponse.success(
            "Retrieved " + reports.size() + " reports.",
            reports
        ));
    }

    // -------------------------------------------------------
    // GET REPORTS BY VILLAGE
    // -------------------------------------------------------

    @GetMapping("/reports/village/{villageId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HEALTH_WORKER', 'GOVERNMENT_OFFICER')")
    @Operation(summary = "Get symptom reports for a specific village")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getReportsByVillage(
            @PathVariable String villageId) {

        List<Map<String, Object>> reports = symptomReportService.getByVillage(villageId);
        return ResponseEntity.ok(ApiResponse.success(
            "Found " + reports.size() + " reports for village " + villageId,
            reports
        ));
    }

    // -------------------------------------------------------
    // GET REPORT BY ID
    // -------------------------------------------------------

    @GetMapping("/report/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HEALTH_WORKER', 'GOVERNMENT_OFFICER')")
    @Operation(summary = "Get a specific symptom report by ID")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReportById(@PathVariable String id) {
        return symptomReportService.getById(id)
            .map(r -> ResponseEntity.ok(ApiResponse.success("Report found.", r)))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Report not found: " + id, 404)));
    }

    // -------------------------------------------------------
    // REVIEW REPORT
    // -------------------------------------------------------

    @PutMapping("/report/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'HEALTH_WORKER')")
    @Operation(summary = "Review and update a symptom report status")
    public ResponseEntity<ApiResponse<String>> reviewReport(
            @PathVariable String id,
            @RequestParam String status,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal NeerVedaUserPrincipal principal) {

        String reviewedBy = principal != null ? principal.getUsername() : "system";
        symptomReportService.reviewReport(id, status, notes, reviewedBy);

        return ResponseEntity.ok(ApiResponse.success(
            "Report " + id + " updated to status: " + status,
            "Updated"
        ));
    }
}
