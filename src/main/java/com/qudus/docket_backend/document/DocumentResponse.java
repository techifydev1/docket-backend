package com.qudus.docket_backend.document;

public record DocumentResponse(String id, String encryptedMetadata, int keyVersion, String addedBy, String addedAt, String ownerId, String publicId, String cloudinaryVersion) {
}
