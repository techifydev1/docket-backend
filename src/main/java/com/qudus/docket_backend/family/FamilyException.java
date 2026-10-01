package com.qudus.docket_backend.family;

public class FamilyException extends RuntimeException{
    private final int statusCode;
    private final String errorCode;
    public FamilyException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}