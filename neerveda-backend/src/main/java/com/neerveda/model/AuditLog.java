package com.neerveda.model;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 📋 AuditLog — Tracks all security-relevant events.
 *
 * Events tracked: LOGIN, LOGOUT, FAILED_LOGIN, DATA_MODIFY, ALERT_GENERATED
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    private String id;
    private String userId;
    private String userEmail;
    private EventType eventType;
    private String description;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private boolean success;

    public enum EventType {
        LOGIN,
        LOGOUT,
        FAILED_LOGIN,
        PASSWORD_RESET,
        DATA_CREATE,
        DATA_UPDATE,
        DATA_DELETE,
        ALERT_GENERATED,
        ALERT_ACKNOWLEDGED,
        USER_CREATED,
        USER_UPDATED,
        SENSOR_READING,
        SYMPTOM_REPORT
    }
}
