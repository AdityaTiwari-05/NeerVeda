package com.neerveda.controller;

import com.neerveda.dto.ApiResponse;
import com.neerveda.dto.PredictionResponse;
import com.neerveda.service.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 🤖 PredictionController
 *
 * AI/ML disease outbreak and water quality prediction endpoints.
 *
 * BASE URL: /api/v1/predictions
 */
@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
@Tag(name = "AI Predictions", description = "AI/ML-based disease outbreak and water quality trend prediction")
@SecurityRequirement(name = "bearerAuth")
public class PredictionController {

    private final PredictionService predictionService;

    @GetMapping("/village/{villageId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOVERNMENT_OFFICER', 'HEALTH_WORKER')")
    @Operation(
        summary = "Get AI prediction for a village",
        description = "Returns disease outbreak probability, water quality trend, anomaly detection, and recommendations"
    )
    public ResponseEntity<ApiResponse<PredictionResponse>> predictForVillage(
            @PathVariable String villageId) {

        PredictionResponse prediction = predictionService.predictForVillage(villageId);
        return ResponseEntity.ok(ApiResponse.success(
            "Prediction generated for village " + villageId,
            prediction
        ));
    }
}
