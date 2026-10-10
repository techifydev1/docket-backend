package com.qudus.docket_backend.invitation;

public record InvitationResponse(String id, String familyId, String familyName, String inviterEmail, String status, String createdAt) {
}
