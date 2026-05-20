package com.neerveda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 📦 ApiResponse - Standard Response Wrapper
 *
 * Every API in NeerVeda returns data in this format:
 *
 * {
 *   "success": true,
 *   "message": "Water quality data saved successfully",
 *   "data": { ... actual data ... },
 *   "timestamp": "2026-01-13T10:30:00"
 * }
 *
 * This makes it easy for the frontend to always know
 * whether a request succeeded or failed.
 *
 * @param <T> - The type of data being returned
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    // Was the request successful?
    private boolean success;

    // Human-readable message
    private String message;

    // The actual data (can be any type - sensor reading, alert, list, etc.)
    private T data;

    // When this response was generated
    private LocalDateTime timestamp;

    // HTTP status code (200, 201, 400, 500, etc.)
    private int statusCode;

    // ===== HELPER METHODS =====

    // Quick way to create a success response
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();
    }

    // Quick way to create an error response
    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .statusCode(statusCode)
                .build();
    }

    // Quick way to create a "created" response (HTTP 201)
    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(201)
                .build();
    }
}
