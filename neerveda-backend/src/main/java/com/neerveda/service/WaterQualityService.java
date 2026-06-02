package com.neerveda.service;

import com.neerveda.model.Alert;
import com.neerveda.model.WaterQualityData;
import com.neerveda.model.WaterQualityData.WaterStatus;
import com.neerveda.repository.FirestoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 💧 WaterQualityService
 *
 * Core water safety analysis engine.
 *
 * Responsibilities:
 *  1. Validate incoming IoT sensor data
 *  2. Assess water quality against WHO / Jal Jeevan Mission thresholds
 *  3. Persist readings to Firestore "water_readings" collection
 *  4. Trigger alert generation when thresholds are breached
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WaterQualityService {

    private static final String COLLECTION = "water_readings";

    private final FirestoreRepository firestoreRepository;
    private final AlertService alertService;
    private final SmsService smsService;

    @Value("${neerveda.threshold.ph.min}")
    private double phMin;

    @Value("${neerveda.threshold.ph.max}")
    private double phMax;

    @Value("${neerveda.threshold.tds.max}")
    private double tdsMax;

    @Value("${neerveda.threshold.turbidity.max}")
    private double turbidityMax;

    @Value("${neerveda.threshold.temperature.max}")
    private double temperatureMax;

    // -------------------------------------------------------
    // ANALYSE & PERSIST
    // -------------------------------------------------------

    public WaterQualityData analyzeAndSave(WaterQualityData data) {
        data.setId(UUID.randomUUID().toString());
        data.setTimestamp(LocalDateTime.now());

        analyzeWaterQuality(data);

        // Persist to Firestore
        firestoreRepository.save(COLLECTION, data.getId(), toMap(data));

        // Trigger alert pipeline if dangerous
        if (data.getStatus() == WaterStatus.DANGER) {
            alertService.createWaterQualityAlert(data);
            smsService.sendWaterAlert(data);
        }

        log.info("📊 Reading saved [{}] — Village: {} | Status: {}",
            data.getId(), data.getVillageName(), data.getStatus());

        return data;
    }

    // -------------------------------------------------------
    // ANALYSIS LOGIC (also used by tests)
    // -------------------------------------------------------

    public WaterQualityData analyzeWaterQuality(WaterQualityData data) {
        if (data.getId() == null) data.setId(UUID.randomUUID().toString());
        if (data.getTimestamp() == null) data.setTimestamp(LocalDateTime.now());

        StringBuilder alertMessage = new StringBuilder();
        List<String> alertParams = new ArrayList<>();
        WaterStatus status = WaterStatus.SAFE;

        // pH check
        if (data.getPh() < phMin || data.getPh() > phMax) {
            status = WaterStatus.DANGER;
            alertParams.add("pH");
            alertMessage.append(String.format(
                "pH %.1f outside safe range (%.1f–%.1f). Risk: Gastroenteritis. ",
                data.getPh(), phMin, phMax));
        }

        // TDS check
        if (data.getTds() > tdsMax) {
            status = WaterStatus.DANGER;
            alertParams.add("TDS");
            alertMessage.append(String.format(
                "TDS %.0f ppm exceeds limit (%.0f ppm). Risk: Kidney disease. ",
                data.getTds(), tdsMax));
        }

        // Turbidity check
        if (data.getTurbidity() > turbidityMax) {
            status = WaterStatus.DANGER;
            alertParams.add("Turbidity");
            alertMessage.append(String.format(
                "Turbidity %.1f NTU exceeds limit (%.0f NTU). Risk: Cholera, Typhoid. ",
                data.getTurbidity(), turbidityMax));
        }

        // Temperature check
        if (data.getTemperature() > temperatureMax) {
            if (status != WaterStatus.DANGER) status = WaterStatus.WARNING;
            alertParams.add("Temperature");
            alertMessage.append(String.format(
                "Temperature %.1f°C exceeds limit (%.0f°C). Risk: Bacterial growth. ",
                data.getTemperature(), temperatureMax));
        }

        data.setStatus(status);
        data.setAlertParameter(String.join(", ", alertParams));
        data.setAlertMessage(
            status == WaterStatus.SAFE
                ? "✅ All parameters within safe limits."
                : alertMessage.toString().trim()
        );

        return data;
    }

    // -------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------

    public List<Map<String, Object>> getAllReadings() {
        return firestoreRepository.findAll(COLLECTION);
    }

    public Optional<Map<String, Object>> getReadingById(String id) {
        return firestoreRepository.findById(COLLECTION, id);
    }

    public List<Map<String, Object>> getReadingsByVillage(String villageId) {
        return firestoreRepository.findByField(COLLECTION, "villageId", villageId);
    }

    public List<Map<String, Object>> getDangerousReadings() {
        return firestoreRepository.findByField(COLLECTION, "status", "DANGER");
    }

    // -------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------

    public boolean requiresImmediateAlert(WaterQualityData data) {
        return data.getStatus() == WaterStatus.DANGER;
    }

    public String generateSmsAlert(WaterQualityData data) {
        return String.format(
            "NeerVeda ALERT: Unsafe water in %s. Issue: %s. " +
            "Avoid drinking. Contact health worker. -NeerVeda",
            data.getVillageName(), data.getAlertParameter()
        );
    }

    private Map<String, Object> toMap(WaterQualityData d) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", d.getId());
        m.put("villageId", d.getVillageId());
        m.put("villageName", d.getVillageName());
        m.put("district", d.getDistrict());
        m.put("state", d.getState());
        m.put("latitude", d.getLatitude());
        m.put("longitude", d.getLongitude());
        m.put("ph", d.getPh());
        m.put("tds", d.getTds());
        m.put("turbidity", d.getTurbidity());
        m.put("temperature", d.getTemperature());
        m.put("status", d.getStatus() != null ? d.getStatus().name() : null);
        m.put("alertParameter", d.getAlertParameter());
        m.put("alertMessage", d.getAlertMessage());
        m.put("deviceId", d.getDeviceId());
        m.put("timestamp", d.getTimestamp() != null ? d.getTimestamp().toString() : null);
        return m;
    }
}
