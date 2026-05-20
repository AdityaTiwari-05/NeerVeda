package com.neerveda;

import com.neerveda.model.WaterQualityData;
import com.neerveda.model.WaterQualityData.WaterStatus;
import com.neerveda.service.WaterQualityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 🧪 NeerVeda Water Quality Service Tests
 *
 * These tests verify that our water safety analysis logic works correctly.
 * Run these with: mvn test
 */
@SpringBootTest
class WaterQualityServiceTest {

    @Autowired
    private WaterQualityService waterQualityService;

    // Sample water data for testing
    private WaterQualityData safeWaterData;
    private WaterQualityData unsafeWaterData;

    @BeforeEach
    void setUp() {
        // SAFE water - all values within limits
        safeWaterData = WaterQualityData.builder()
                .villageId("VIL001")
                .villageName("Dimapur Village")
                .district("Dimapur")
                .state("Nagaland")
                .ph(7.2)           // Safe: 6.5 - 8.5
                .tds(250.0)        // Safe: below 500 ppm
                .turbidity(2.0)    // Safe: below 5 NTU
                .temperature(25.0) // Safe: below 35°C
                .deviceId("ESP32-001")
                .build();

        // UNSAFE water - pH and turbidity out of range
        unsafeWaterData = WaterQualityData.builder()
                .villageId("VIL002")
                .villageName("Kohima Village")
                .district("Kohima")
                .state("Nagaland")
                .ph(5.8)           // UNSAFE: below 6.5
                .tds(300.0)        // Safe
                .turbidity(8.5)    // UNSAFE: above 5 NTU
                .temperature(28.0) // Safe
                .deviceId("ESP32-002")
                .build();
    }

    @Test
    @DisplayName("Safe water should return SAFE status")
    void testSafeWaterAnalysis() {
        WaterQualityData result = waterQualityService.analyzeWaterQuality(safeWaterData);

        assertEquals(WaterStatus.SAFE, result.getStatus());
        assertNotNull(result.getId());
        assertNotNull(result.getTimestamp());
        assertTrue(result.getAlertMessage().contains("✅"));
    }

    @Test
    @DisplayName("Unsafe water should return DANGER status")
    void testUnsafeWaterAnalysis() {
        WaterQualityData result = waterQualityService.analyzeWaterQuality(unsafeWaterData);

        assertEquals(WaterStatus.DANGER, result.getStatus());
        assertNotNull(result.getAlertParameter());
        assertTrue(result.getAlertMessage().contains("⚠️"));
    }

    @Test
    @DisplayName("DANGER water should require immediate alert")
    void testImmediateAlertForDangerousWater() {
        WaterQualityData result = waterQualityService.analyzeWaterQuality(unsafeWaterData);

        assertTrue(waterQualityService.requiresImmediateAlert(result));
    }

    @Test
    @DisplayName("SAFE water should NOT require immediate alert")
    void testNoAlertForSafeWater() {
        WaterQualityData result = waterQualityService.analyzeWaterQuality(safeWaterData);

        assertFalse(waterQualityService.requiresImmediateAlert(result));
    }

    @Test
    @DisplayName("SMS alert message should mention village name")
    void testSmsAlertMessage() {
        WaterQualityData result = waterQualityService.analyzeWaterQuality(unsafeWaterData);
        String sms = waterQualityService.generateSmsAlert(result);

        assertTrue(sms.contains("Kohima Village"));
        assertTrue(sms.contains("NeerVeda"));
    }
}
