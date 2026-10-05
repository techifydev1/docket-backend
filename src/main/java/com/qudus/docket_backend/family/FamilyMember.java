package com.qudus.docket_backend.family;

import java.time.Instant;

public record FamilyMember(String name, String userId, Role role, String joinedAt, String profilePic) {
    public FamilyMemberResponse toResponse() {
        return new FamilyMemberResponse(name, userId, profilePic);
    }
}
