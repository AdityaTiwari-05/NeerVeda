package com.neerveda.dto;

import com.neerveda.model.User;
import lombok.*;

/**
 * 🔓 AuthResponse DTO — Returned on successful login/register.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;        // seconds

    private String userId;
    private String name;
    private String email;
    private User.Role role;
}
