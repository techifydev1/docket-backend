package com.qudus.docket_backend.user;

import java.time.Instant;
import java.util.Objects;

public final class User {
    private String userId;
    private String fullName;
    private String phone;
    private String email;
    private String profilePic;
    private Instant createdAt;
    private boolean emailVerified;
    private String publicKey;

    public User() {}

    public User(String userId, String fullName, String phone, String email, String profilePic, Instant createdAt, boolean emailVerified, String publicKey) {
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.profilePic = profilePic;
        this.createdAt = createdAt;
        this.emailVerified = emailVerified;
        this.publicKey = publicKey;
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

    public String getPublicKey() {
        return publicKey;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public UserResponse toResponse() {
        return new UserResponse(fullName, userId, email, phone, profilePic, createdAt, emailVerified);
    }

}