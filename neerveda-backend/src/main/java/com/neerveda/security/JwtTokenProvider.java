package com.neerveda.security;

import com.neerveda.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * 🔐 JwtTokenProvider
 *
 * Handles JWT access token and refresh token generation,
 * validation, and claims extraction.
 *
 * Security:
 *  - Uses HMAC-SHA256 with a 256-bit secret
 *  - Access token: 15 minutes
 *  - Refresh token: 7 days
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${neerveda.jwt.secret}")
    private String jwtSecret;

    @Value("${neerveda.jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${neerveda.jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    // -------------------------------------------------------
    // TOKEN GENERATION
    // -------------------------------------------------------

    public String generateAccessToken(Authentication authentication) {
        NeerVedaUserPrincipal principal = (NeerVedaUserPrincipal) authentication.getPrincipal();
        return buildToken(principal.getUser(), accessTokenExpiry, "access");
    }

    public String generateAccessTokenFromUser(User user) {
        return buildToken(user, accessTokenExpiry, "access");
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, refreshTokenExpiry, "refresh");
    }

    private String buildToken(User user, long expiry, String type) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiry);

        return Jwts.builder()
                .subject(user.getId())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("name", user.getName())
                .claim("type", type)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // -------------------------------------------------------
    // TOKEN VALIDATION
    // -------------------------------------------------------

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token expired: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("JWT token malformed: {}", ex.getMessage());
        } catch (SecurityException ex) {
            log.warn("JWT signature invalid: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims empty: {}", ex.getMessage());
        }
        return false;
    }

    // -------------------------------------------------------
    // CLAIMS EXTRACTION
    // -------------------------------------------------------

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUserIdFromToken(String token) {
        return extractClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public String getEmailFromToken(String token) {
        return extractClaims(token).get("email", String.class);
    }

    public String getTokenType(String token) {
        return extractClaims(token).get("type", String.class);
    }

    // -------------------------------------------------------
    // KEY
    // -------------------------------------------------------

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
            java.util.Base64.getEncoder()
                .encodeToString(jwtSecret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
