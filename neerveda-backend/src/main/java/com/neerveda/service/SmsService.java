package com.neerveda.service;

import com.neerveda.model.WaterQualityData;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * 📱 SmsService
 *
 * Twilio-based SMS alert service.
 * Sends alerts to health workers and government officers.
 *
 * Set twilio.enabled=true and provide real credentials
 * via environment variables to activate SMS delivery.
 */
@Slf4j
@Service
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    @Value("${twilio.enabled:false}")
    private boolean twilioEnabled;

    @PostConstruct
    public void init() {
        if (twilioEnabled) {
            Twilio.init(accountSid, authToken);
            log.info("📱 Twilio SMS service initialized.");
        } else {
            log.info("📱 Twilio SMS service is DISABLED (set twilio.enabled=true to activate).");
        }
    }

    // -------------------------------------------------------
    // SEND WATER ALERT
    // -------------------------------------------------------

    public void sendWaterAlert(WaterQualityData data) {
        String message = String.format(
            "[NeerVeda ALERT] Unsafe water detected in %s, %s.\n" +
            "Issue: %s\n" +
            "DO NOT drink from local sources. Contact health officer immediately.",
            data.getVillageName(), data.getDistrict(),
            data.getAlertParameter()
        );
        sendToAlertRecipients(message);
    }

    public void sendOutbreakAlert(String villageName, String disease, int count) {
        String message = String.format(
            "[NeerVeda OUTBREAK ALERT] %d %s cases reported in %s.\n" +
            "Immediate public health response required.",
            count, disease, villageName
        );
        sendToAlertRecipients(message);
    }

    public void sendToNumber(String toNumber, String message) {
        if (!twilioEnabled) {
            log.info("📱 [SMS SIMULATION] To: {} | Message: {}", toNumber, message);
            return;
        }

        try {
            Message.creator(
                new PhoneNumber(toNumber),
                new PhoneNumber(fromNumber),
                message
            ).create();
            log.info("✅ SMS sent to {}", toNumber);
        } catch (Exception e) {
            log.error("❌ Failed to send SMS to {}: {}", toNumber, e.getMessage());
        }
    }

    // -------------------------------------------------------
    // PRIVATE HELPERS
    // -------------------------------------------------------

    private void sendToAlertRecipients(String message) {
        // In production, load recipient phone numbers from the users collection
        // For now, log the message
        log.warn("📱 ALERT SMS: {}", message);
    }
}
