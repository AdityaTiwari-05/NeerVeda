package com.neerveda.model;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 👤 User Model — RBAC roles for NeerVeda platform.
 *
 * Roles:
 *   ADMIN             — Full system access
 *   GOVERNMENT_OFFICER — View reports, manage alerts
 *   HEALTH_WORKER      — Submit / view symptom reports
 *   WATER_INSPECTOR    — Submit / view sensor readings
 *   PUBLIC_VIEWER      — Read-only public dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String id;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean active;
    private String phone;
    private String district;
    private String state;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private String refreshToken;

    public enum Role {
        ADMIN,
        GOVERNMENT_OFFICER,
        HEALTH_WORKER,
        WATER_INSPECTOR,
        PUBLIC_VIEWER
    }
}
