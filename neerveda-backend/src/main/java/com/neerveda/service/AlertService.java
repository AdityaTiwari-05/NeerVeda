package com.neerveda.service;

import com.neerveda.model.Alert;
import com.neerveda.model.WaterQualityData;
import com.neerveda.repository.FirestoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 🚨 AlertService
 *
 * Creates and manages system alerts.
 * Alerts are stored in Firestore "alerts" collection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private static final String COLLECTION = "alerts";

    private final FirestoreRepository firestoreRepository;

    // -------------------------------------------------------
    // CREATE ALERTS
    // -------------------------------------------------------

    public Alert createWaterQualityAlert(WaterQualityData data) {
        Alert.AlertSeverity severity = determineSeverity(data);

        Alert alert = Alert.builder()
            .id(UUID.randomUUID().toString())
            .alertType(Alert.AlertType.WATER_QUALITY)
            .severity(severity)
            .villageId(data.getVillageId())
            .villageName(data.getVillageName())
            .district(data.getDistrict())
            .state(data.getState())
            .latitude(data.getLatitude())
            .longitude(data.getLongitude())
            .title("Water Quality Alert — " + data.getVillageName())
            .message(data.getAlertMessage())
            .affectedParameter(data.getAlertParameter())
            .status("ACTIVE")
            .createdAt(LocalDateTime.now())
            .build();

        firestoreRepository.save(COLLECTION, alert.getId(), alertToMap(alert));
        log.warn("🚨 Alert created [{}] for village: {} | Severity: {}",
            alert.getId(), data.getVillageName(), severity);

        return alert;
    }

    public Alert createOutbreakAlert(String villageId, String villageName,
                                      String district, String state,
                                      double lat, double lon,
                                      String diseaseName, int reportCount) {
        Alert alert = Alert.builder()
            .id(UUID.randomUUID().toString())
            .alertType(Alert.AlertType.DISEASE_OUTBREAK)
            .severity(Alert.AlertSeverity.HIGH)
            .villageId(villageId)
            .villageName(villageName)
            .district(district)
            .state(state)
            .latitude(lat)
            .longitude(lon)
            .title("Disease Outbreak Risk — " + villageName)
            .message(String.format(
                "%d symptom reports of %s in %s within 48 hours. Immediate action required.",
                reportCount, diseaseName, villageName))
            .status("ACTIVE")
            .createdAt(LocalDateTime.now())
            .build();

        firestoreRepository.save(COLLECTION, alert.getId(), alertToMap(alert));
        log.warn("🦠 Outbreak alert [{}] for village: {}", alert.getId(), villageName);
        return alert;
    }

    // -------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------

    public List<Map<String, Object>> getAllAlerts() {
        return firestoreRepository.findAll(COLLECTION);
    }

    public List<Map<String, Object>> getActiveAlerts() {
        return firestoreRepository.findByField(COLLECTION, "status", "ACTIVE");
    }

    public List<Map<String, Object>> getAlertsByVillage(String villageId) {
        return firestoreRepository.findByField(COLLECTION, "villageId", villageId);
    }

    // -------------------------------------------------------
    // STATUS UPDATES
    // -------------------------------------------------------

    public void acknowledgeAlert(String id, String acknowledgedBy) {
        firestoreRepository.update(COLLECTION, id, Map.of(
            "status", "ACKNOWLEDGED",
            "resolvedBy", acknowledgedBy,
            "resolvedAt", LocalDateTime.now().toString()
        ));
        log.info("✅ Alert {} acknowledged by {}", id, acknowledgedBy);
    }

    public void resolveAlert(String id, String resolvedBy) {
        firestoreRepository.update(COLLECTION, id, Map.of(
            "status", "RESOLVED",
            "resolvedBy", resolvedBy,
            "resolvedAt", LocalDateTime.now().toString()
        ));
        log.info("✅ Alert {} resolved by {}", id, resolvedBy);
    }

    // -------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------

    private Alert.AlertSeverity determineSeverity(WaterQualityData data) {
        int violations = 0;
        if (data.getPh() < 6.5 || data.getPh() > 8.5) violations++;
        if (data.getTds() > 500) violations++;
        if (data.getTurbidity() > 5) violations++;
        if (data.getTemperature() > 35) violations++;

        return switch (violations) {
            case 1 -> Alert.AlertSeverity.MEDIUM;
            case 2 -> Alert.AlertSeverity.HIGH;
            default -> Alert.AlertSeverity.CRITICAL;
        };
    }

    private Map<String, Object> alertToMap(Alert a) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("alertType", a.getAlertType() != null ? a.getAlertType().name() : null);
        m.put("severity", a.getSeverity() != null ? a.getSeverity().name() : null);
        m.put("villageId", a.getVillageId());
        m.put("villageName", a.getVillageName());
        m.put("district", a.getDistrict());
        m.put("state", a.getState());
        m.put("latitude", a.getLatitude());
        m.put("longitude", a.getLongitude());
        m.put("title", a.getTitle());
        m.put("message", a.getMessage());
        m.put("affectedParameter", a.getAffectedParameter());
        m.put("status", a.getStatus());
        m.put("createdAt", a.getCreatedAt() != null ? a.getCreatedAt().toString() : null);
        return m;
    }
}
