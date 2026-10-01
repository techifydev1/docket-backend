package com.qudus.docket_backend.family;

import java.time.Instant;

public record FamilyMember(String userId, Role role, String joinedAt) {
}
