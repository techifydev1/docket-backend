package com.qudus.docket_backend.invitation;

import org.springframework.http.HttpStatus;

public class InvitationException extends RuntimeException{
    private HttpStatus status;
    private String errorCode;


    public InvitationException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
