package com.qudus.docket_backend.family;


import java.util.List;
import java.util.Map;

public record FamilyResponse(String name, String id, String pic, long memberCount, String createdAt, List<FamilyMemberResponse> familyMembers, Map<String, String> wrappedKeys) {
}
