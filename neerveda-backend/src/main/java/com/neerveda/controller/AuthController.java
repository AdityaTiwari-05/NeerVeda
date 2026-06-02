package com.neerveda.controller;

import com.neerveda.dto.*;
import com.neerveda.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 🔐 AuthController
 *
 * Handles user authentication.
 *
 * BASE URL: /api/v1/auth
 * Endpoints:
 *   POST /login
 *   POST /register
 *   POST /refresh
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, register, and token refresh endpoints")
public class AuthController {

    private final AuthService authService;

    // -------------------------------------------------------
    // LOGIN
    // -------------------------------------------------------

    @PostMapping("/login")
    @Operation(summary = "Login with email and password", description = "Returns JWT access and refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.login(
            request,
            httpRequest.getRemoteAddr(),
            httpRequest.getHeader("User-Agent")
        );

        return ResponseEntity.ok(ApiResponse.success("Login successful.", response));
    }

    // -------------------------------------------------------
    // REGISTER
    // -------------------------------------------------------

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.register(
            request,
            httpRequest.getRemoteAddr(),
            httpRequest.getHeader("User-Agent")
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.created("Account created successfully.", response));
    }

    // -------------------------------------------------------
    // REFRESH TOKEN
    // -------------------------------------------------------

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestParam String refreshToken) {

        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed.", response));
    }

    // -------------------------------------------------------
    // HEALTH CHECK
    // -------------------------------------------------------

    @GetMapping("/health")
    @Operation(summary = "Auth service health check")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success(
            "Auth service is running.",
            "OK — " + LocalDateTime.now()
        ));
    }
}
