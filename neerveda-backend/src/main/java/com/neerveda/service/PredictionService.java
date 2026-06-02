package com.neerveda.service;

import com.neerveda.dto.PredictionResponse;
import com.neerveda.repository.FirestoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 🤖 PredictionService — AI/ML-based disease outbreak and water quality prediction.
 *
 * Algorithms implemented:
 *  1. Random Forest-like heuristic scoring (disease outbreak probability)
 *  2. ARIMA-like trend detection (water quality trend)
 *  3. Isolation Forest-like anomaly detection (sensor anomalies)
 *
 * Note: For SIH prototype, these are deterministic heuristic models
 * that produce meaningful outputs based on real data patterns.
 * Production version would integrate a Python ML microservice via HTTP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private static final String MODEL_VERSION = "heuristic-v1.0-SIH25001";

    private final FirestoreRepository firestoreRepository;

    // -------------------------------------------------------
    // PREDICT FOR A VILLAGE
    // -------------------------------------------------------

    public PredictionResponse predictForVillage(String villageId) {
        // Load recent readings (last 10)
        List<Map<String, Object>> readings = firestoreRepository
            .findRecentByField("water_readings", "villageId", villageId, "timestamp", 10);

        // Load recent symptom reports (last 20)
        List<Map<String, Object>> reports = firestoreRepository
            .findRecentByField("symptom_reports", "villageId", villageId, "timestamp", 20);

        // Extract village meta from latest reading
        String villageName = readings.isEmpty() ? villageId :
            (String) readings.get(0).getOrDefault("villageName", villageId);
        String district = readings.isEmpty() ? "" :
            (String) readings.get(0).getOrDefault("district", "");
        String state = readings.isEmpty() ? "" :
            (String) readings.get(0).getOrDefault("state", "");

        // Run prediction models
        double outbreakProb = computeOutbreakProbability(readings, reports);
        double riskScore = computeRiskScore(readings, reports, outbreakProb);
        boolean anomaly = detectAnomaly(readings);
        String trend = detectWaterQualityTrend(readings);

        PredictionResponse.RiskLevel riskLevel = classifyRisk(riskScore);

        List<String> diseases = predictDiseases(readings, reports);
        List<String> recommendations = generateRecommendations(riskLevel, readings, reports);

        return PredictionResponse.builder()
            .villageId(villageId)
            .villageName(villageName)
            .district(district)
            .state(state)
            .overallRiskLevel(riskLevel)
            .overallRiskScore(riskScore)
            .outbreakProbability(outbreakProb)
            .predictedDiseases(diseases)
            .outbreakTimeframe("next 7 days")
            .waterQualityTrend(trend)
            .anomalyDetected(anomaly)
            .anomalyDescription(anomaly ? "Sensor readings deviate significantly from historical baseline." : null)
            .recommendations(recommendations)
            .immediateAction(riskLevel == PredictionResponse.RiskLevel.CRITICAL
                ? "Issue immediate public health advisory and deploy response team."
                : null)
            .generatedAt(LocalDateTime.now())
            .modelVersion(MODEL_VERSION)
            .build();
    }

    // -------------------------------------------------------
    // MODEL: Outbreak Probability (Random Forest heuristic)
    // -------------------------------------------------------

    private double computeOutbreakProbability(
            List<Map<String, Object>> readings,
            List<Map<String, Object>> reports) {

        double score = 0.0;

        // Factor 1: Number of recent symptom reports (weight: 0.4)
        int recentReports = reports.size();
        score += Math.min(recentReports / 10.0, 1.0) * 0.4;

        // Factor 2: Severe symptom reports (weight: 0.2)
        long severeCount = reports.stream()
            .filter(r -> "SEVERE".equals(r.get("severity")))
            .count();
        score += Math.min(severeCount / 5.0, 1.0) * 0.2;

        // Factor 3: Water quality violations (weight: 0.3)
        long dangerCount = readings.stream()
            .filter(r -> "DANGER".equals(r.get("status")))
            .count();
        if (!readings.isEmpty()) {
            score += (double) dangerCount / readings.size() * 0.3;
        }

        // Factor 4: Turbidity elevation (weight: 0.1)
        OptionalDouble avgTurbidity = readings.stream()
            .mapToDouble(r -> toDouble(r.get("turbidity")))
            .average();
        if (avgTurbidity.isPresent() && avgTurbidity.getAsDouble() > 5.0) {
            score += 0.1;
        }

        return Math.min(score, 1.0);
    }

    // -------------------------------------------------------
    // MODEL: Overall Risk Score
    // -------------------------------------------------------

    private double computeRiskScore(List<Map<String, Object>> readings,
                                     List<Map<String, Object>> reports,
                                     double outbreakProb) {
        double waterScore = 0.0;

        if (!readings.isEmpty()) {
            Map<String, Object> latest = readings.get(0);
            double ph = toDouble(latest.get("ph"));
            double tds = toDouble(latest.get("tds"));
            double turbidity = toDouble(latest.get("turbidity"));
            double temp = toDouble(latest.get("temperature"));

            // Normalize each parameter to 0–1 violation score
            double phViolation = (ph < 6.5 || ph > 8.5) ? Math.abs(ph - 7.0) / 7.0 : 0;
            double tdsViolation = tds > 500 ? Math.min((tds - 500) / 500.0, 1.0) : 0;
            double turbViolation = turbidity > 5 ? Math.min((turbidity - 5) / 20.0, 1.0) : 0;
            double tempViolation = temp > 35 ? Math.min((temp - 35) / 20.0, 1.0) : 0;

            waterScore = (phViolation + tdsViolation + turbViolation + tempViolation) / 4.0;
        }

        return (waterScore * 0.6 + outbreakProb * 0.4);
    }

    // -------------------------------------------------------
    // MODEL: Anomaly Detection (Isolation Forest heuristic)
    // -------------------------------------------------------

    private boolean detectAnomaly(List<Map<String, Object>> readings) {
        if (readings.size() < 3) return false;

        // Calculate mean and std for pH
        DoubleSummaryStatistics phStats = readings.stream()
            .mapToDouble(r -> toDouble(r.get("ph")))
            .summaryStatistics();

        double mean = phStats.getAverage();
        double range = phStats.getMax() - phStats.getMin();

        // Sudden large spike is an anomaly
        double latest = toDouble(readings.get(0).get("ph"));
        return Math.abs(latest - mean) > 2.0 || range > 3.0;
    }

    // -------------------------------------------------------
    // MODEL: Water Quality Trend (ARIMA heuristic)
    // -------------------------------------------------------

    private String detectWaterQualityTrend(List<Map<String, Object>> readings) {
        if (readings.size() < 2) return "STABLE";

        // Compare average danger rate in first half vs second half
        int mid = readings.size() / 2;
        long recentDanger = readings.subList(0, mid).stream()
            .filter(r -> "DANGER".equals(r.get("status"))).count();
        long olderDanger = readings.subList(mid, readings.size()).stream()
            .filter(r -> "DANGER".equals(r.get("status"))).count();

        if (recentDanger > olderDanger) return "DEGRADING";
        if (recentDanger < olderDanger) return "IMPROVING";
        return "STABLE";
    }

    // -------------------------------------------------------
    // DISEASE PREDICTION
    // -------------------------------------------------------

    private List<String> predictDiseases(List<Map<String, Object>> readings,
                                          List<Map<String, Object>> reports) {
        Set<String> diseases = new LinkedHashSet<>();

        if (!readings.isEmpty()) {
            Map<String, Object> latest = readings.get(0);
            double turbidity = toDouble(latest.get("turbidity"));
            double ph = toDouble(latest.get("ph"));
            double temp = toDouble(latest.get("temperature"));

            if (turbidity > 5) {
                diseases.add("Cholera");
                diseases.add("Typhoid");
                diseases.add("Acute Diarrhea");
            }
            if (ph < 6.5) diseases.add("Gastroenteritis");
            if (temp > 35) diseases.add("Hepatitis A");
        }

        // Add from recent report patterns
        reports.stream()
            .map(r -> (String) r.get("suspectedDisease"))
            .filter(Objects::nonNull)
            .forEach(diseases::add);

        return new ArrayList<>(diseases);
    }

    // -------------------------------------------------------
    // RECOMMENDATIONS
    // -------------------------------------------------------

    private List<String> generateRecommendations(
            PredictionResponse.RiskLevel level,
            List<Map<String, Object>> readings,
            List<Map<String, Object>> reports) {

        List<String> recs = new ArrayList<>();

        recs.add("Continue regular water quality monitoring every 4 hours.");

        if (level == PredictionResponse.RiskLevel.CRITICAL || level == PredictionResponse.RiskLevel.HIGH) {
            recs.add("Deploy emergency water purification units immediately.");
            recs.add("Issue boil-water advisory to all residents.");
            recs.add("Activate ASHA worker network for household visits.");
            recs.add("Notify District Magistrate and Health Officer.");
        } else if (level == PredictionResponse.RiskLevel.MEDIUM) {
            recs.add("Increase monitoring frequency to every 2 hours.");
            recs.add("Inspect and chlorinate water sources.");
            recs.add("Distribute water purification tablets to vulnerable households.");
        } else {
            recs.add("Maintain current water treatment protocols.");
            recs.add("Conduct monthly pipeline inspection.");
        }

        return recs;
    }

    // -------------------------------------------------------
    // RISK CLASSIFICATION
    // -------------------------------------------------------

    private PredictionResponse.RiskLevel classifyRisk(double score) {
        if (score >= 0.75) return PredictionResponse.RiskLevel.CRITICAL;
        if (score >= 0.5)  return PredictionResponse.RiskLevel.HIGH;
        if (score >= 0.25) return PredictionResponse.RiskLevel.MEDIUM;
        return PredictionResponse.RiskLevel.LOW;
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(val.toString()); }
        catch (NumberFormatException e) { return 0.0; }
    }
}
