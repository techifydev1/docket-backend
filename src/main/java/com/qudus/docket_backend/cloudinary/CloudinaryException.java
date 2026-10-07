package com.qudus.docket_backend.cloudinary;

import org.springframework.http.HttpStatus;

public class CloudinaryException extends RuntimeException{
    private final String errorCode;
    private final HttpStatus status;


    public CloudinaryException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
