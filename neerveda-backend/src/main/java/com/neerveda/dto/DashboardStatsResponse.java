package com.neerveda.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 📊 DashboardStatsResponse DTO — Admin dashboard summary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    // Summary cards
    private long totalVillages;
    private long totalAlerts;
    private long activeAlerts;
    private long activeSensors;
    private double waterSafetyIndex;     // 0 – 100

    // Breakdown
    private long safeVillages;
    private long warningVillages;
    private long dangerVillages;

    // Symptom stats
    private long totalSymptomReports;
    private long pendingReviews;
    private long outbreakRiskVillages;

    // Trends (last 30 days)
    private List<Map<String, Object>> monthlyWaterQuality;
    private List<Map<String, Object>> diseaseTrends;
    private List<Map<String, Object>> sensorActivity;

    private LocalDateTime generatedAt;
}
