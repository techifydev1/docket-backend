package com.qudus.docket_backend.document;

public record ConfirmRequest(String familyId, String docId, String cloudinaryVersion, String signature, String encryptedMetadata, String kv) {
}
