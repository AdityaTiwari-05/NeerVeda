package com.neerveda.service;

import com.neerveda.model.SymptomReport;
import com.neerveda.repository.FirestoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 🏥 SymptomReportService
 *
 * Manages disease symptom reports.
 * Detects outbreak patterns: if ≥N reports of same suspected disease
 * arrive from same village within 48 hours, raises an outbreak alert.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SymptomReportService {

    private static final String COLLECTION = "symptom_reports";

    private final FirestoreRepository firestoreRepository;
    private final AlertService alertService;

    @Value("${neerveda.alert.outbreak.threshold:3}")
    private int outbreakThreshold;

    // -------------------------------------------------------
    // SUBMIT
    // -------------------------------------------------------

    public SymptomReport submit(SymptomReport report) {
        report.setId(UUID.randomUUID().toString());
        report.setTimestamp(LocalDateTime.now());
        report.setStatus("PENDING");

        firestoreRepository.save(COLLECTION, report.getId(), toMap(report));
        log.info("🏥 Symptom report [{}] from {} in {}", report.getId(),
            report.getReportedBy(), report.getVillageName());

        checkOutbreakThreshold(report);
        return report;
    }

    // -------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------

    public List<Map<String, Object>> getAll() {
        return firestoreRepository.findAll(COLLECTION);
    }

    public List<Map<String, Object>> getByVillage(String villageId) {
        return firestoreRepository.findByField(COLLECTION, "villageId", villageId);
    }

    public Optional<Map<String, Object>> getById(String id) {
        return firestoreRepository.findById(COLLECTION, id);
    }

    // -------------------------------------------------------
    // REVIEW
    // -------------------------------------------------------

    public void reviewReport(String id, String status, String notes, String reviewedBy) {
        firestoreRepository.update(COLLECTION, id, Map.of(
            "status", status,
            "reviewNotes", notes != null ? notes : "",
            "reviewedBy", reviewedBy
        ));
        log.info("📝 Report {} reviewed by {} → {}", id, reviewedBy, status);
    }

    // -------------------------------------------------------
    // OUTBREAK DETECTION
    // -------------------------------------------------------

    private void checkOutbreakThreshold(SymptomReport report) {
        if (report.getSuspectedDisease() == null) return;

        List<Map<String, Object>> villageReports =
            firestoreRepository.findByField(COLLECTION, "villageId", report.getVillageId());

        // Count reports of same disease in last 48 hours
        LocalDateTime cutoff = LocalDateTime.now().minusHours(48);
        long recentCount = villageReports.stream()
            .filter(r -> report.getSuspectedDisease().equals(r.get("suspectedDisease")))
            .filter(r -> {
                try {
                    String ts = (String) r.get("timestamp");
                    return ts != null && LocalDateTime.parse(ts).isAfter(cutoff);
                } catch (Exception e) { return false; }
            })
            .count();

        if (recentCount >= outbreakThreshold) {
            log.warn("🦠 Outbreak threshold reached for {} in {}",
                report.getSuspectedDisease(), report.getVillageName());
            alertService.createOutbreakAlert(
                report.getVillageId(), report.getVillageName(),
                report.getDistrict(), report.getState(),
                report.getLatitude(), report.getLongitude(),
                report.getSuspectedDisease(), (int) recentCount
            );
        }
    }

    // -------------------------------------------------------
    // MAPPING
    // -------------------------------------------------------

    private Map<String, Object> toMap(SymptomReport r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("reportedBy", r.getReportedBy());
        m.put("reporterRole", r.getReporterRole());
        m.put("reporterPhone", r.getReporterPhone());
        m.put("patientAge", r.getPatientAge());
        m.put("patientGender", r.getPatientGender());
        m.put("villageId", r.getVillageId());
        m.put("villageName", r.getVillageName());
        m.put("district", r.getDistrict());
        m.put("state", r.getState());
        m.put("latitude", r.getLatitude());
        m.put("longitude", r.getLongitude());
        m.put("symptoms", r.getSymptoms());
        m.put("suspectedDisease", r.getSuspectedDisease());
        m.put("severity", r.getSeverity());
        m.put("affectedCount", r.getAffectedCount());
        m.put("waterSource", r.getWaterSource());
        m.put("status", r.getStatus());
        m.put("timestamp", r.getTimestamp() != null ? r.getTimestamp().toString() : null);
        return m;
    }
}
