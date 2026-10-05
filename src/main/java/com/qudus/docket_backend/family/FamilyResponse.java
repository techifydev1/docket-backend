package com.qudus.docket_backend.family;


import java.util.List;

public record FamilyResponse(String name, String id, String pic, long memberCount, String createdAt, List<FamilyMemberResponse> familyMembers) {
}
