package com.qudus.docket_backend.auth;

public record AuthRequest(String vaultName, String fullName, String email, String phone, Boolean isBiometricsEnabled, String publicKey, String wrappedFamilyKey) {
    public AuthRequest {
        if (isBiometricsEnabled == null) {
            isBiometricsEnabled = false;
        }
    }
}
