package com.lifeguard.model;

import java.time.Instant;

/**
 * Standardised error response — Java 21 record with compact constructor for validation.
 */
public record ApiError(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp
) {
    /** Compact constructor — validates invariants on construction. */
    public ApiError {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("ApiError message must not be blank");
        }
        if (timestamp == null) timestamp = Instant.now();
    }

    /** Convenience factory. */
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(status, error, message, path, Instant.now());
    }
}
