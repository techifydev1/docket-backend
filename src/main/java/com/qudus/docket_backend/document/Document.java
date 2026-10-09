package com.qudus.docket_backend.document;

public class Document {
    private String encryptedMetadata;
    private int keyVersion;
    private String addedBy;
    private String addedAt;
    private String ownerId;
    private String publicId;
    private String cloudinaryVersion;

    public Document(String encryptedMetadata, int keyVersion, String addedBy, String addedAt, String ownerId, String publicId, String cloudinaryVersion) {
        this.encryptedMetadata = encryptedMetadata;
        this.keyVersion = keyVersion;
        this.addedBy = addedBy;
        this.addedAt = addedAt;
        this.ownerId = ownerId;
        this.publicId = publicId;
        this.cloudinaryVersion = cloudinaryVersion;
    }

    public Document() {
    }

    public String getEncryptedMetadata() {
        return encryptedMetadata;
    }

    public long getKeyVersion() {
        return keyVersion;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public String getAddedAt() {
        return addedAt;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getPublicId() {
        return publicId;
    }

    public String getCloudinaryVersion() {
        return cloudinaryVersion;
    }
}
