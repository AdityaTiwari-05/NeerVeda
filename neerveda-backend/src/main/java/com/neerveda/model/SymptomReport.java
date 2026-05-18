package com.neerveda.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 🏥 SymptomReport Model
 *
 * Represents a disease symptom report submitted by:
 * - ASHA Workers
 * - Volunteers
 * - Clinics
 * - Community members (via mobile/SMS)
 *
 * This feeds into the AI/ML outbreak prediction model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymptomReport {

    // Unique report ID
    private String id;

    // Who submitted this report
    private String reportedBy;       // Name of ASHA worker / volunteer
    private String reporterRole;     // ASHA_WORKER, VOLUNTEER, CLINIC, COMMUNITY
    private String reporterPhone;    // For SMS-based reporting

    // Patient information (anonymous for privacy)
    private int patientAge;
    private String patientGender;    // MALE, FEMALE, OTHER

    // Location
    private String villageId;
    private String villageName;
    private String district;
    private String state;
    private double latitude;
    private double longitude;

    // ===== SYMPTOMS =====
    // List of symptoms reported (e.g., ["DIARRHEA", "VOMITING", "FEVER"])
    private List<String> symptoms;

    // Possible disease based on symptoms
    private String suspectedDisease; // e.g., "CHOLERA", "TYPHOID", "HEPATITIS_A"

    // Severity: MILD, MODERATE, SEVERE
    private String severity;

    // How many people in the household are affected
    private int affectedCount;

    // Did the patient consume water from a known source?
    private String waterSource;      // WELL, TAP, RIVER, POND, OTHER

    // ===== STATUS =====
    private String status;           // PENDING, REVIEWED, RESOLVED

    // When the report was submitted
    private LocalDateTime timestamp;

    // Notes from health officer
    private String reviewNotes;
}
