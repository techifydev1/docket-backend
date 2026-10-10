package com.qudus.docket_backend.invitation;

public record InviteRequest(String inviteeEmail, String familyId, String inviteePublicKey, String wrappedFamilyKey) {
}
