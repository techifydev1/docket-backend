package com.qudus.docket_backend.user;

import java.time.Instant;

public record UserResponse(String fullName, String id, String email, String phone, String profilePic, Instant createdAt, boolean emailVerified) {
}
