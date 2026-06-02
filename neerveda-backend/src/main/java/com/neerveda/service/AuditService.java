package com.neerveda.service;

import com.neerveda.model.AuditLog;
import com.neerveda.repository.FirestoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 📋 AuditService — Tracks all security-relevant actions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private static final String COLLECTION = "audit_logs";

    private final FirestoreRepository firestoreRepository;

    public void log(String userId, String userEmail,
                    AuditLog.EventType eventType, String description,
                    String ipAddress, String userAgent, boolean success) {
        try {
            String id = UUID.randomUUID().toString();
            AuditLog auditLog = AuditLog.builder()
                .id(id)
                .userId(userId)
                .userEmail(userEmail)
                .eventType(eventType)
                .description(description)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .success(success)
                .build();

            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("userId", userId);
            map.put("userEmail", userEmail);
            map.put("eventType", eventType.name());
            map.put("description", description);
            map.put("ipAddress", ipAddress);
            map.put("userAgent", userAgent);
            map.put("timestamp", auditLog.getTimestamp().toString());
            map.put("success", success);

            firestoreRepository.save(COLLECTION, id, map);
        } catch (Exception e) {
            // Audit logging must never throw — just log to console
            log.error("❌ Failed to persist audit log: {}", e.getMessage());
        }
    }

    public List<Map<String, Object>> getAll() {
        return firestoreRepository.findAll(COLLECTION);
    }

    public List<Map<String, Object>> getByUser(String userId) {
        return firestoreRepository.findByField(COLLECTION, "userId", userId);
    }
}
