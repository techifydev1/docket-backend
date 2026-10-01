package com.qudus.docket_backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ErrorResponse(String errorCode, String errorMessage, long statusCode, List<Map<String, Object>> fieldErrors, Instant timestamp) {
    public static ErrorResponse of(String errorCode, String errorMessage) {
        return new ErrorResponse(errorCode, errorMessage, HttpStatus.UNAUTHORIZED.value(), null, Instant.now());
    }

    public static ErrorResponse validation(List<Map<String, Object>> errors) {
        return new ErrorResponse("failed_validation", "One or more field's validation failed", HttpStatus.BAD_REQUEST.value(), errors, Instant.now());
    }
}
