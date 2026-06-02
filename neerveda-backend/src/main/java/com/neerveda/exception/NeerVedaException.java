package com.neerveda.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 🚨 NeerVedaException — Base application exception.
 *
 * Carries HTTP status and error code for structured error responses.
 */
@Getter
public class NeerVedaException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public NeerVedaException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public NeerVedaException(String message, HttpStatus status) {
        this(message, status, status.name());
    }

    // Factory methods for common cases
    public static NeerVedaException notFound(String entity, String id) {
        return new NeerVedaException(
            entity + " not found with ID: " + id,
            HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND"
        );
    }

    public static NeerVedaException unauthorized(String message) {
        return new NeerVedaException(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    public static NeerVedaException forbidden(String message) {
        return new NeerVedaException(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    public static NeerVedaException conflict(String message) {
        return new NeerVedaException(message, HttpStatus.CONFLICT, "CONFLICT");
    }

    public static NeerVedaException badRequest(String message) {
        return new NeerVedaException(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }
}
