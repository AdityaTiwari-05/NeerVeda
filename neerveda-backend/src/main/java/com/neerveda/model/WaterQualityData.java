package com.neerveda.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 💧 WaterQualityData Model
 *
 * This class represents ONE reading from an IoT water sensor.
 * Each time the ESP32 sensor sends data, it will be stored
 * as a WaterQualityData object in Firebase.
 *
 * SENSORS USED (from NeerVeda PPT):
 * - pH Sensor        → measures acidity/alkalinity
 * - TDS Sensor       → Total Dissolved Solids (ppm)
 * - Turbidity Sensor → water cloudiness (NTU)
 * - Temperature Sensor → water temperature (°C)
 */
@Data               // Lombok: auto-generates getters, setters, toString
@Builder            // Lombok: lets us use WaterQualityData.builder().ph(7.0).build()
@NoArgsConstructor  // Lombok: generates empty constructor
@AllArgsConstructor // Lombok: generates constructor with all fields
public class WaterQualityData {

    // Unique ID for each reading (Firebase will generate this)
    private String id;

    // Which village/location this sensor is installed in
    private String villageId;
    private String villageName;
    private String district;
    private String state;

    // GPS coordinates of the sensor
    private double latitude;
    private double longitude;

    // ===== SENSOR READINGS =====

    // pH value (Safe range: 6.5 - 8.5)
    // Below 6.5 or above 8.5 → Gastroenteritis risk
    private double ph;

    // TDS - Total Dissolved Solids in ppm (Safe: below 500 ppm)
    // Above 500 ppm → Kidney problems risk
    private double tds;

    // Turbidity in NTU (Safe: below 5 NTU)
    // Above 5 NTU → Cholera, Typhoid, Diarrhea risk
    private double turbidity;

    // Water temperature in Celsius (Safe: below 35°C)
    // Above 35°C or sudden change → Bacterial growth, Cholera, Hepatitis A
    private double temperature;

    // ===== STATUS =====

    // Overall water quality status: SAFE, WARNING, DANGER
    private WaterStatus status;

    // Which parameter caused the alert (e.g., "pH", "TDS", "Turbidity")
    private String alertParameter;

    // Human-readable alert message
    private String alertMessage;

    // When this reading was taken
    private LocalDateTime timestamp;

    // Which device/sensor sent this data
    private String deviceId;

    // ===== ENUM for Status =====
    public enum WaterStatus {
        SAFE,       // All parameters within safe limits
        WARNING,    // One parameter is borderline
        DANGER      // One or more parameters exceed safe limits
    }
}
