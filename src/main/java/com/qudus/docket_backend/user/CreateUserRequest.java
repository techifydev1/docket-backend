package com.qudus.docket_backend.user;

public record CreateUserRequest(String fullName, String email, String phone, boolean biometricsEnabled) {
}
