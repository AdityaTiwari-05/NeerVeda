package com.neerveda.service;

import com.neerveda.dto.*;
import com.neerveda.exception.NeerVedaException;
import com.neerveda.model.AuditLog;
import com.neerveda.model.User;
import com.neerveda.security.JwtTokenProvider;
import com.neerveda.security.NeerVedaUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * 🔐 AuthService
 *
 * Handles login, register, token refresh, and logout.
 * Issues JWT access + refresh tokens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final AuditService auditService;

    private static final long ACCESS_EXPIRY_SECONDS = 900L;

    // -------------------------------------------------------
    // LOGIN
    // -------------------------------------------------------

    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(), request.getPassword())
            );

            NeerVedaUserPrincipal principal = (NeerVedaUserPrincipal) authentication.getPrincipal();
            User user = principal.getUser();

            String accessToken  = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            userService.updateRefreshToken(user.getId(), refreshToken);
            userService.updateLastLogin(user.getId());

            auditService.log(user.getId(), user.getEmail(),
                AuditLog.EventType.LOGIN, "Successful login", ipAddress, userAgent, true);

            log.info("✅ Login: {} ({})", user.getEmail(), user.getRole());

            return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(ACCESS_EXPIRY_SECONDS)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        } catch (Exception e) {
            auditService.log(null, request.getEmail(),
                AuditLog.EventType.FAILED_LOGIN,
                "Failed login attempt", ipAddress, userAgent, false);
            log.warn("❌ Failed login attempt for: {}", request.getEmail());
            throw NeerVedaException.unauthorized("Invalid email or password.");
        }
    }

    // -------------------------------------------------------
    // REGISTER
    // -------------------------------------------------------

    public AuthResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        User newUser = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .passwordHash(request.getPassword()) // UserService will BCrypt encode
            .role(request.getRole())
            .phone(request.getPhone())
            .district(request.getDistrict())
            .state(request.getState())
            .build();

        User saved = userService.createUser(newUser);

        auditService.log(saved.getId(), saved.getEmail(),
            AuditLog.EventType.USER_CREATED,
            "New user registered with role: " + saved.getRole(),
            ipAddress, userAgent, true);

        // Auto-login after registration
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(request.getEmail());
        loginReq.setPassword(request.getPassword());
        return login(loginReq, ipAddress, userAgent);
    }

    // -------------------------------------------------------
    // REFRESH TOKEN
    // -------------------------------------------------------

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw NeerVedaException.unauthorized("Refresh token is invalid or expired.");
        }

        if (!"refresh".equals(jwtTokenProvider.getTokenType(refreshToken))) {
            throw NeerVedaException.unauthorized("Invalid token type.");
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userService.findById(userId)
            .orElseThrow(() -> NeerVedaException.unauthorized("User not found."));

        String newAccessToken  = jwtTokenProvider.generateAccessTokenFromUser(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        userService.updateRefreshToken(userId, newRefreshToken);

        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(ACCESS_EXPIRY_SECONDS)
            .userId(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
    }
}
