package com.neerveda.service;

import com.neerveda.model.WaterQualityData;
import com.neerveda.model.WaterQualityData.WaterStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 💧 WaterQualityService
 *
 * This is the BRAIN for water quality analysis.
 * It receives raw sensor data and:
 * 1. Checks each parameter against safe thresholds
 * 2. Determines the overall water status (SAFE/WARNING/DANGER)
 * 3. Generates alert messages if needed
 * 4. Saves data to Firebase (coming in next phase)
 *
 * Thresholds are based on:
 * - WHO Drinking Water Guidelines
 * - Jal Jeevan Mission WQMS Framework
 */
@Service  // Tells Spring this is a service class
public class WaterQualityService {

    // These values come from application.properties
    // So we can change thresholds without changing code!
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

    /**
     * Analyzes incoming sensor data and determines water safety status
     *
     * @param data - Raw sensor reading from ESP32
     * @return Analyzed WaterQualityData with status and alert info
     */
    public WaterQualityData analyzeWaterQuality(WaterQualityData data) {

        // Generate unique ID and set timestamp
        data.setId(UUID.randomUUID().toString());
        data.setTimestamp(LocalDateTime.now());

        // Analyze each sensor parameter
        StringBuilder alertMessage = new StringBuilder();
        String alertParameter = null;
        WaterStatus status = WaterStatus.SAFE; // Start optimistic!

        // ===== CHECK pH =====
        // Safe range: 6.5 - 8.5
        if (data.getPh() < phMin || data.getPh() > phMax) {
            status = WaterStatus.DANGER;
            alertParameter = "pH";
            alertMessage.append(String.format(
                "⚠️ pH level (%.1f) is outside safe range (%.1f - %.1f). " +
                "Risk: Gastroenteritis, Skin Irritation. ",
                data.getPh(), phMin, phMax
            ));
        }

        // ===== CHECK TDS =====
        // Safe limit: below 500 ppm
        if (data.getTds() > tdsMax) {
            status = WaterStatus.DANGER;
            alertParameter = alertParameter == null ? "TDS" : alertParameter + ", TDS";
            alertMessage.append(String.format(
                "⚠️ TDS level (%.0f ppm) exceeds safe limit (%.0f ppm). " +
                "Risk: Kidney problems. ",
                data.getTds(), tdsMax
            ));
        }

        // ===== CHECK TURBIDITY =====
        // Safe limit: below 5 NTU
        if (data.getTurbidity() > turbidityMax) {
            status = WaterStatus.DANGER;
            alertParameter = alertParameter == null ? "Turbidity" : alertParameter + ", Turbidity";
            alertMessage.append(String.format(
                "⚠️ Turbidity (%.1f NTU) exceeds safe limit (%.0f NTU). " +
                "Risk: Cholera, Typhoid, Acute Diarrhea. ",
                data.getTurbidity(), turbidityMax
            ));
        }

        // ===== CHECK TEMPERATURE =====
        // Safe limit: below 35°C
        if (data.getTemperature() > temperatureMax) {
            // Temperature alone is WARNING unless combined with others
            if (status == WaterStatus.DANGER) {
                status = WaterStatus.DANGER;
            } else {
                status = WaterStatus.WARNING;
            }
            alertParameter = alertParameter == null ? "Temperature" : alertParameter + ", Temperature";
            alertMessage.append(String.format(
                "⚠️ Temperature (%.1f°C) exceeds safe limit (%.0f°C). " +
                "Risk: Bacterial growth, Cholera, Hepatitis A. ",
                data.getTemperature(), temperatureMax
            ));
        }

        // ===== SET STATUS =====
        data.setStatus(status);
        data.setAlertParameter(alertParameter);
        data.setAlertMessage(
            status == WaterStatus.SAFE
                ? "✅ All water quality parameters are within safe limits."
                : alertMessage.toString().trim()
        );

        return data;
    }

    /**
     * Checks if a water reading requires an immediate alert
     * (to be sent via SMS/notification)
     */
    public boolean requiresImmediateAlert(WaterQualityData data) {
        return data.getStatus() == WaterStatus.DANGER;
    }

    /**
     * Generates a short SMS-friendly alert message (for Twilio)
     * Kept under 160 characters for single SMS
     */
    public String generateSmsAlert(WaterQualityData data) {
        return String.format(
            "NeerVeda ALERT: Unsafe water detected in %s. " +
            "Issue: %s. Avoid drinking water. Contact health worker immediately.",
            data.getVillageName(),
            data.getAlertParameter()
        );
    }
}
