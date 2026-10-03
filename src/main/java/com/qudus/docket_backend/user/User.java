package com.qudus.docket_backend.user;

import java.time.Instant;
import java.util.Objects;

public final class User {
    private final String userId;
    private final String fullName;
    private final String phone;
    private final String email;
    private final String profilePic;
    private final Instant createdAt;
    private final boolean emailVerified;

    public User(String userId, String fullName, String phone, String email, String profilePic, Instant createdAt, boolean emailVerified) {
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.profilePic = profilePic;
        this.createdAt = createdAt;
        this.emailVerified = emailVerified;
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public UserResponse toResponse() {
        return new UserResponse(fullName, userId, email, phone, profilePic, createdAt, emailVerified);
    }

}