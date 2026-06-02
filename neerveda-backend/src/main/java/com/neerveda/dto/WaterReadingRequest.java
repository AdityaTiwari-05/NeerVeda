package com.neerveda.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 📡 WaterReadingRequest DTO — IoT sensor data from ESP32.
 *
 * Validates all incoming sensor values against physically plausible ranges.
 * Out-of-range sensor values likely indicate device malfunction.
 */
@Data
public class WaterReadingRequest {

    @NotBlank(message = "Village ID is required")
    private String villageId;

    @NotBlank(message = "Village name is required")
    private String villageName;

    private String district;
    private String state;

    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    private double latitude;

    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    private double longitude;

    @NotNull(message = "pH value is required")
    @DecimalMin(value = "0.0", message = "pH cannot be negative")
    @DecimalMax(value = "14.0", message = "pH cannot exceed 14")
    private Double ph;

    @NotNull(message = "TDS value is required")
    @DecimalMin(value = "0.0", message = "TDS cannot be negative")
    @DecimalMax(value = "5000.0", message = "TDS value out of sensor range")
    private Double tds;

    @NotNull(message = "Turbidity value is required")
    @DecimalMin(value = "0.0", message = "Turbidity cannot be negative")
    @DecimalMax(value = "1000.0", message = "Turbidity value out of sensor range")
    private Double turbidity;

    @NotNull(message = "Temperature value is required")
    @DecimalMin(value = "-10.0", message = "Temperature below sensor range")
    @DecimalMax(value = "100.0", message = "Temperature above sensor range")
    private Double temperature;

    @NotBlank(message = "Device ID is required")
    private String deviceId;
}
