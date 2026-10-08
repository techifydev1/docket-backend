package com.qudus.docket_backend.document;

public class Document {
    private String meta;
    private int kv;
    private String by;
    private String at;

    public Document(String meta, int kv, String by, String at) {
        this.meta = meta;
        this.kv = kv;
        this.by = by;
        this.at = at;
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
}
