package com.neerveda;

import com.neerveda.model.WaterQualityData;
import com.neerveda.model.WaterQualityData.WaterStatus;
import com.neerveda.service.AlertService;
import com.neerveda.service.SmsService;
import com.neerveda.service.WaterQualityService;
import com.neerveda.repository.FirestoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 🧪 WaterQualityServiceTest
 *
 * Unit tests for the core water quality analysis engine.
 * Tests all threshold logic, alert generation, and SMS helper.
 *
 * Run with: mvn test
 */
@ExtendWith(MockitoExtension.class)
class WaterQualityServiceTest {

    @Mock
    private FirestoreRepository firestoreRepository;

    @Mock
    private AlertService alertService;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private WaterQualityService waterQualityService;

    @BeforeEach
    void setUp() {
        // Inject threshold values (normally from application.properties)
        ReflectionTestUtils.setField(waterQualityService, "phMin", 6.5);
        ReflectionTestUtils.setField(waterQualityService, "phMax", 8.5);
        ReflectionTestUtils.setField(waterQualityService, "tdsMax", 500.0);
        ReflectionTestUtils.setField(waterQualityService, "turbidityMax", 5.0);
        ReflectionTestUtils.setField(waterQualityService, "temperatureMax", 35.0);
    }

    private WaterQualityData safeData() {
        return WaterQualityData.builder()
            .villageId("VIL001")
            .villageName("Dimapur Village")
            .district("Dimapur")
            .state("Nagaland")
            .ph(7.2)
            .tds(250.0)
            .turbidity(2.0)
            .temperature(25.0)
            .deviceId("ESP32-001")
            .build();
    }

    private WaterQualityData dangerData() {
        return WaterQualityData.builder()
            .villageId("VIL002")
            .villageName("Kohima Village")
            .district("Kohima")
            .state("Nagaland")
            .ph(5.8)           // UNSAFE
            .tds(300.0)
            .turbidity(8.5)   // UNSAFE
            .temperature(28.0)
            .deviceId("ESP32-002")
            .build();
    }

    // -------------------------------------------------------
    // SAFE WATER
    // -------------------------------------------------------

    @Test
    @DisplayName("Safe water parameters should return SAFE status")
    void safeWaterReturnsSafeStatus() {
        WaterQualityData result = waterQualityService.analyzeWaterQuality(safeData());
        assertEquals(WaterStatus.SAFE, result.getStatus());
    }

    @Test
    @DisplayName("Safe water should have an ID assigned")
    void safeWaterHasId() {
        WaterQualityData result = waterQualityService.analyzeWaterQuality(safeData());
        assertNotNull(result.getId());
        assertFalse(result.getId().isBlank());
    }

    @Test
    @DisplayName("Safe water should have a timestamp assigned")
    void safeWaterHasTimestamp() {
        WaterQualityData result = waterQualityService.analyzeWaterQuality(safeData());
        assertNotNull(result.getTimestamp());
    }

    @Test
    @DisplayName("Safe water alert message should contain checkmark")
    void safeWaterMessageContainsCheckmark() {
        WaterQualityData result = waterQualityService.analyzeWaterQuality(safeData());
        assertTrue(result.getAlertMessage().contains("✅"));
    }

    @Test
    @DisplayName("Safe water should NOT require immediate alert")
    void safeWaterDoesNotRequireAlert() {
        WaterQualityData result = waterQualityService.analyzeWaterQuality(safeData());
        assertFalse(waterQualityService.requiresImmediateAlert(result));
    }

    // -------------------------------------------------------
    // DANGEROUS WATER
    // -------------------------------------------------------

    @Test
    @DisplayName("Unsafe pH and turbidity should return DANGER status")
    void unsafeParametersReturnDanger() {
        WaterQualityData result = waterQualityService.analyzeWaterQuality(dangerData());
        assertEquals(WaterStatus.DANGER, result.getStatus());
    }

    @Test
    @DisplayName("DANGER water should require immediate alert")
    void dangerWaterRequiresAlert() {
        WaterQualityData result = waterQualityService.analyzeWaterQuality(dangerData());
        assertTrue(waterQualityService.requiresImmediateAlert(result));
    }

    @Test
    @DisplayName("Alert parameter should mention pH")
    void dangerAlertParameterContainsPh() {
        WaterQualityData result = waterQualityService.analyzeWaterQuality(dangerData());
        assertNotNull(result.getAlertParameter());
        assertTrue(result.getAlertParameter().contains("pH"));
    }

    @Test
    @DisplayName("Alert parameter should mention Turbidity")
    void dangerAlertParameterContainsTurbidity() {
        WaterQualityData result = waterQualityService.analyzeWaterQuality(dangerData());
        assertTrue(result.getAlertParameter().contains("Turbidity"));
    }

    // -------------------------------------------------------
    // HIGH TDS
    // -------------------------------------------------------

    @Test
    @DisplayName("TDS above 500 ppm should trigger DANGER")
    void highTdsReturnsDanger() {
        WaterQualityData data = safeData();
        data.setTds(750.0);
        WaterQualityData result = waterQualityService.analyzeWaterQuality(data);
        assertEquals(WaterStatus.DANGER, result.getStatus());
        assertTrue(result.getAlertParameter().contains("TDS"));
    }

    // -------------------------------------------------------
    // HIGH TEMPERATURE (WARNING only)
    // -------------------------------------------------------

    @Test
    @DisplayName("Temperature above 35°C alone should produce WARNING not DANGER")
    void highTemperatureAloneProducesWarning() {
        WaterQualityData data = safeData();
        data.setTemperature(38.0);
        WaterQualityData result = waterQualityService.analyzeWaterQuality(data);
        assertEquals(WaterStatus.WARNING, result.getStatus());
    }

    // -------------------------------------------------------
    // SMS
    // -------------------------------------------------------

    @Test
    @DisplayName("SMS alert should include village name")
    void smsAlertIncludesVillageName() {
        WaterQualityData result = waterQualityService.analyzeWaterQuality(dangerData());
        String sms = waterQualityService.generateSmsAlert(result);
        assertTrue(sms.contains("Kohima Village"));
        assertTrue(sms.contains("NeerVeda"));
    }

    @Test
    @DisplayName("SMS alert should be within 320 character limit (2 SMS)")
    void smsAlertWithinCharacterLimit() {
        WaterQualityData result = waterQualityService.analyzeWaterQuality(dangerData());
        String sms = waterQualityService.generateSmsAlert(result);
        assertTrue(sms.length() <= 320,
            "SMS too long: " + sms.length() + " chars");
    }
}
