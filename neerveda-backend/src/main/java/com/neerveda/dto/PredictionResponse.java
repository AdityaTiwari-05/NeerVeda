package com.neerveda.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 🤖 PredictionResponse DTO — AI/ML prediction results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {

    private String villageId;
    private String villageName;
    private String district;
    private String state;

    // Overall risk
    private RiskLevel overallRiskLevel;
    private double overallRiskScore;       // 0.0 – 1.0

    // Disease outbreak prediction
    private double outbreakProbability;    // 0.0 – 1.0
    private List<String> predictedDiseases;
    private String outbreakTimeframe;      // e.g. "next 7 days"

    // Water quality forecast
    private Map<String, Double> predictedValues; // next 24h avg
    private String waterQualityTrend;             // IMPROVING, STABLE, DEGRADING

    // Anomaly detection
    private boolean anomalyDetected;
    private String anomalyDescription;

    // Recommendations
    private List<String> recommendations;
    private String immediateAction;

    private LocalDateTime generatedAt;
    private String modelVersion;

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
