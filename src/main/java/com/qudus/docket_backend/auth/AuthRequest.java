package com.qudus.docket_backend.auth;

public record AuthRequest(String vaultName, String fullName, String email, String phone, boolean isBiometricsEnabled) {
}
