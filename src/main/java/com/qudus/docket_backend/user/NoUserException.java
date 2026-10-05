package com.qudus.docket_backend.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class NoUserException extends RuntimeException{
    public NoUserException(String message) {
        super(message);
    }
}
