package com.neerveda.service;

import com.neerveda.dto.DashboardStatsResponse;
import com.neerveda.repository.FirestoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 📊 DashboardService — Aggregates statistics for admin dashboard.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FirestoreRepository firestoreRepository;

    public DashboardStatsResponse getAdminStats() {
        List<Map<String, Object>> allReadings = firestoreRepository.findAll("water_readings");
        List<Map<String, Object>> allAlerts   = firestoreRepository.findAll("alerts");
        List<Map<String, Object>> allReports  = firestoreRepository.findAll("symptom_reports");

        // Village counts by status
        Map<String, String> latestByVillage = new LinkedHashMap<>();
        for (Map<String, Object> r : allReadings) {
            String vid = (String) r.get("villageId");
            if (vid != null) {
                latestByVillage.put(vid, (String) r.getOrDefault("status", "SAFE"));
            }
        }

        long totalVillages  = latestByVillage.size();
        long safeVillages   = latestByVillage.values().stream().filter("SAFE"::equals).count();
        long warnVillages   = latestByVillage.values().stream().filter("WARNING"::equals).count();
        long dangerVillages = latestByVillage.values().stream().filter("DANGER"::equals).count();

        long totalAlerts  = allAlerts.size();
        long activeAlerts = allAlerts.stream()
            .filter(a -> "ACTIVE".equals(a.get("status"))).count();

        long pendingReports = allReports.stream()
            .filter(r -> "PENDING".equals(r.get("status"))).count();

        // Water safety index: 100 * (safe villages / total) — clamp 0–100
        double wsi = totalVillages > 0
            ? Math.round((double) safeVillages / totalVillages * 100.0 * 10) / 10.0
            : 100.0;

        // Unique active sensors (unique deviceIds)
        long activeSensors = allReadings.stream()
            .map(r -> r.get("deviceId"))
            .filter(Objects::nonNull)
            .distinct()
            .count();

        return DashboardStatsResponse.builder()
            .totalVillages(totalVillages)
            .totalAlerts(totalAlerts)
            .activeAlerts(activeAlerts)
            .activeSensors(activeSensors)
            .waterSafetyIndex(wsi)
            .safeVillages(safeVillages)
            .warningVillages(warnVillages)
            .dangerVillages(dangerVillages)
            .totalSymptomReports(allReports.size())
            .pendingReviews(pendingReports)
            .outbreakRiskVillages(dangerVillages)
            .monthlyWaterQuality(buildMonthlyTrend(allReadings))
            .diseaseTrends(buildDiseaseTrend(allReports))
            .sensorActivity(buildSensorActivity(allReadings))
            .generatedAt(LocalDateTime.now())
            .build();
    }

    // -------------------------------------------------------
    // TREND BUILDERS
    // -------------------------------------------------------

    private List<Map<String, Object>> buildMonthlyTrend(List<Map<String, Object>> readings) {
        // Group by day (last 30 days), count safe vs danger
        Map<String, long[]> byDay = new LinkedHashMap<>();
        for (Map<String, Object> r : readings) {
            String ts = (String) r.get("timestamp");
            if (ts == null) continue;
            try {
                String day = ts.substring(0, 10);
                byDay.computeIfAbsent(day, k -> new long[]{0, 0});
                if ("DANGER".equals(r.get("status"))) byDay.get(day)[1]++;
                else byDay.get(day)[0]++;
            } catch (Exception ignored) {}
        }
        return byDay.entrySet().stream().map(e -> {
            Map<String, Object> point = new HashMap<>();
            point.put("date", e.getKey());
            point.put("safe", e.getValue()[0]);
            point.put("danger", e.getValue()[1]);
            return point;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildDiseaseTrend(List<Map<String, Object>> reports) {
        Map<String, Long> counts = reports.stream()
            .map(r -> (String) r.get("suspectedDisease"))
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(d -> d, Collectors.counting()));

        return counts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(e -> Map.of("disease", (Object) e.getKey(), "count", (Object) e.getValue()))
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildSensorActivity(List<Map<String, Object>> readings) {
        Map<String, Long> bySensor = readings.stream()
            .map(r -> (String) r.get("deviceId"))
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(d -> d, Collectors.counting()));

        return bySensor.entrySet().stream()
            .map(e -> Map.of("deviceId", (Object) e.getKey(), "readings", (Object) e.getValue()))
            .collect(Collectors.toList());
    }
}
