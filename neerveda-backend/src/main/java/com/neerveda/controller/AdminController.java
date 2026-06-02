package com.neerveda.controller;

import com.neerveda.dto.ApiResponse;
import com.neerveda.model.User;
import com.neerveda.service.AuditService;
import com.neerveda.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 🛡️ AdminController
 *
 * Admin-only endpoints for user management and audit logs.
 * BASE URL: /api/v1/admin
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only endpoints: user management, audit logs, system settings")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserService userService;
    private final AuditService auditService;

    // -------------------------------------------------------
    // USER MANAGEMENT
    // -------------------------------------------------------

    @GetMapping("/users")
    @Operation(summary = "List all users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.findAll();
        // Mask password hashes before returning
        users.forEach(u -> u.setPasswordHash("[REDACTED]"));
        return ResponseEntity.ok(ApiResponse.success("Retrieved " + users.size() + " users.", users));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get a specific user by ID")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String id) {
        return userService.findById(id)
            .map(u -> {
                u.setPasswordHash("[REDACTED]");
                return ResponseEntity.ok(ApiResponse.success("User found.", u));
            })
            .orElse(ResponseEntity.status(404)
                .body(ApiResponse.error("User not found: " + id, 404)));
    }

    @PutMapping("/users/{id}/deactivate")
    @Operation(summary = "Deactivate a user account")
    public ResponseEntity<ApiResponse<String>> deactivateUser(@PathVariable String id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated.", id));
    }

    // -------------------------------------------------------
    // AUDIT LOGS
    // -------------------------------------------------------

    @GetMapping("/audit-logs")
    @Operation(summary = "Get all audit logs")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAuditLogs() {
        List<Map<String, Object>> logs = auditService.getAll();
        return ResponseEntity.ok(ApiResponse.success(
            "Retrieved " + logs.size() + " audit log entries.",
            logs
        ));
    }

    @GetMapping("/audit-logs/user/{userId}")
    @Operation(summary = "Get audit logs for a specific user")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAuditLogsByUser(
            @PathVariable String userId) {
        List<Map<String, Object>> logs = auditService.getByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(
            "Found " + logs.size() + " log entries for user " + userId,
            logs
        ));
    }
}
