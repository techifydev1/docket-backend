package com.qudus.docket_backend.family;

import java.time.Instant;

public record FamilyResponse(String name, String id, String pic, long memberCount, String createdAt) {
}
