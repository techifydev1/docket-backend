package com.qudus.docket_backend.document;

public class Document {
    private String meta;
    private int kv;
    private String by;
    private String at;
    private String ownerId;

    public Document(String meta, int kv, String by, String at, String ownerId) {
        this.meta = meta;
        this.kv = kv;
        this.by = by;
        this.at = at;
        this.ownerId = ownerId;
    }

    public Document() {
    }

    public String getMeta() {
        return meta;
    }

    public long getKv() {
        return kv;
    }

    public String getBy() {
        return by;
    }

    public String getAt() {
        return at;
    }

    public String getOwnerId() {
        return ownerId;
    }
}
